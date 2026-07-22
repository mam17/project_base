package com.libads.core.internal

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import com.libads.core.provider.AdProvider
import com.libads.core.util.AdLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import kotlin.time.Duration.Companion.milliseconds

internal class AdManagerImpl(
    application: Application
) : AdManager {

    // WeakReference để không leak Application dù thực tế Application sống cả vòng đời app
    private val appRef = WeakReference(application)

    // Scope riêng cho lib, Main dispatcher vì hầu hết callback ads SDK trả về trên main thread
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val providers = mutableMapOf<String, AdProvider>()
    private val cache = AdCache()

    override fun registerProvider(provider: AdProvider) {
        if (providers.containsKey(provider.name)) {
            AdLogger.w("Provider '${provider.name}' đã được đăng ký trước đó, bỏ qua.")
            return
        }
        providers[provider.name] = provider
        appRef.get()?.let { app ->
            provider.initialize(app) { success ->
                AdLogger.d("Provider '${provider.name}' initialize success=$success")
            }
        }
    }

    private fun providerOf(adUnit: AdUnit): AdProvider? {
        val provider = providers[adUnit.providerName]
        if (provider == null) {
            AdLogger.e("Không tìm thấy provider '${adUnit.providerName}' cho adUnit '${adUnit.id}'. Bạn đã registerProvider() chưa?")
        }
        return provider
    }

    override fun preload(adUnit: AdUnit, callback: AdLoadCallback?) {
        val provider = providerOf(adUnit) ?: run {
            callback?.onResult(AdResult.Failure(adUnit.id, ERROR_NO_PROVIDER, "Provider not registered"))
            return
        }
        val context = appRef.get() ?: return

        if (provider.isReady(adUnit)) {
            callback?.onResult(AdResult.Success(adUnit.id))
            return
        }
        if (cache.isLoading(adUnit.id)) {
            AdLogger.d("AdUnit '${adUnit.id}' đang load rồi, bỏ qua request trùng.")
            cache.addCallback(adUnit.id, callback)
            return
        }

        cache.markLoading(adUnit.id)
        cache.addCallback(adUnit.id, callback)
        scope.launch {
            val result = withTimeoutOrNull(adUnit.timeoutMillis.milliseconds) {
                awaitLoad(provider, context.applicationContext, adUnit)
            } ?: AdResult.TimedOut(adUnit.id)

            cache.markIdle(adUnit.id)
            if (result is AdResult.TimedOut) {
                AdLogger.w("AdUnit '${adUnit.id}' load timeout sau ${adUnit.timeoutMillis}ms")
            }
            cache.dispatchCallbacks(adUnit.id, result)
        }
    }

    private suspend fun awaitLoad(
        provider: AdProvider,
        context: android.content.Context,
        adUnit: AdUnit
    ): AdResult = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        provider.load(context, adUnit) { result ->
            if (cont.isActive) cont.resumeWith(Result.success(result))
        }
    }

    override fun isReady(adUnit: AdUnit): Boolean {
        return providerOf(adUnit)?.isReady(adUnit) ?: false
    }

    override fun show(activity: Activity, adUnit: AdUnit, callback: AdShowCallback?) {
        val provider = providerOf(adUnit) ?: run {
            callback?.onAdFailedToShow(ERROR_NO_PROVIDER, "Provider not registered")
            return
        }

        if (provider.isReady(adUnit)) {
            provider.show(activity, adUnit, callback ?: NoopShowCallback)
            return
        }

        // Chưa sẵn sàng: tự load rồi show, có timeout để không treo user vô thời hạn
        AdLogger.d("AdUnit '${adUnit.id}' chưa ready, tự load trước khi show.")
        preload(adUnit) { result ->
            when (result) {
                is AdResult.Success -> provider.show(activity, adUnit, callback ?: NoopShowCallback)
                is AdResult.Failure -> callback?.onAdFailedToShow(result.errorCode, result.message)
                is AdResult.TimedOut -> callback?.onAdFailedToShow(ERROR_TIMEOUT, "Ad load timed out")
            }
        }
    }

    override fun loadAndShow(activity: Activity, adUnit: AdUnit, callback: AdShowCallback?) {
        if (!adUnit.type.supportsLoadAndShow()) {
            callback?.onAdFailedToShow(
                ERROR_UNSUPPORTED_TYPE,
                "loadAndShow only supports interstitial and rewarded ads"
            )
            return
        }

        val provider = providerOf(adUnit) ?: run {
            callback?.onAdFailedToShow(ERROR_NO_PROVIDER, "Provider not registered")
            return
        }

        provider.destroy(adUnit)
        cache.clear(adUnit.id)
        AdLogger.d("AdUnit '${adUnit.id}' loadAndShow: force load before show.")
        preload(adUnit) { result ->
            when (result) {
                is AdResult.Success -> provider.show(activity, adUnit, callback ?: NoopShowCallback)
                is AdResult.Failure -> callback?.onAdFailedToShow(result.errorCode, result.message)
                is AdResult.TimedOut -> callback?.onAdFailedToShow(ERROR_TIMEOUT, "Ad load timed out")
            }
        }
    }

    override fun renderInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback?) {
        val provider = providerOf(adUnit) ?: run {
            callback?.onResult(AdResult.Failure(adUnit.id, ERROR_NO_PROVIDER, "Provider not registered"))
            return
        }
        provider.renderInto(container, adUnit, callback ?: NoopLoadCallback)
    }

    override fun destroy(adUnit: AdUnit) {
        providers[adUnit.providerName]?.destroy(adUnit)
        cache.clear(adUnit.id)
    }

    override fun destroyAll() {
        cache.clearAll()
    }

    private fun AdType.supportsLoadAndShow(): Boolean {
        return this == AdType.INTERSTITIAL ||
            this == AdType.REWARDED ||
            this == AdType.REWARDED_INTERSTITIAL
    }

    private companion object {
        const val ERROR_NO_PROVIDER = -1
        const val ERROR_TIMEOUT = -2
        const val ERROR_UNSUPPORTED_TYPE = -3

        val NoopLoadCallback = AdLoadCallback { }
        val NoopShowCallback = object : AdShowCallback {}
    }
}
