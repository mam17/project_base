package com.libads.core.internal

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdRevenue
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import com.libads.core.provider.AdProvider
import com.libads.core.util.AdLogger
import com.libads.core.util.AdEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

internal class AdManagerImpl(application: Application) : AdManager {

    private val appContext = application.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val providers = ConcurrentHashMap<String, AdProvider>()
    private val cache = AdCache()
    private val knownAdUnits = ConcurrentHashMap<PlacementKey, AdUnit>()
    private val showingFullScreenPlacement = AtomicReference<PlacementKey?>(null)

    override fun registerProvider(provider: AdProvider) {
        if (providers.putIfAbsent(provider.name, provider) != null) {
            AdLogger.w("Provider '${provider.name}' is already registered")
            return
        }
        provider.initialize(appContext) { success ->
            AdLogger.d("Provider '${provider.name}' initialize success=$success")
        }
    }

    override fun preload(adUnit: AdUnit, callback: AdLoadCallback?) {
        val provider = providerOf(adUnit) ?: run {
            val result = noProviderResult(adUnit)
            logLoadResult(adUnit, result)
            callback?.onResult(result)
            return
        }
        prepareAdUnit(provider, adUnit)

        if (provider.isReady(adUnit)) {
            callback?.onResult(AdResult.Success(adUnit.id))
            return
        }

        val registration = cache.register(adUnit, callback)
        if (registration is AdCache.Registration.Joined) {
            AdLogger.d("AdUnit '${adUnit.id}' is already loading; callback joined")
            return
        }

        val requestId = (registration as AdCache.Registration.Started).requestId
        AdLogger.event(adUnit, AdEventType.LOAD_STARTED)
        scope.launch {
            val result = withTimeoutOrNull(adUnit.timeoutMillis.milliseconds) {
                awaitLoad(provider, adUnit)
            } ?: AdResult.TimedOut(adUnit.id)

            if (result is AdResult.TimedOut) {
                provider.destroy(adUnit)
                AdLogger.w("AdUnit '${adUnit.id}' load timed out after ${adUnit.timeoutMillis}ms")
            }
            if (cache.complete(adUnit, requestId, result)) {
                logLoadResult(adUnit, result)
            }
        }
    }

    override fun isLoading(adUnit: AdUnit): Boolean = cache.isLoading(adUnit)

    override fun isReady(adUnit: AdUnit): Boolean {
        val provider = providerOf(adUnit) ?: return false
        prepareAdUnit(provider, adUnit)
        return provider.isReady(adUnit)
    }

    override fun show(activity: Activity, adUnit: AdUnit, callback: AdShowCallback?) {
        scope.launch {
            showInternal(activity, activity as? LifecycleOwner, adUnit, callback)
        }
    }

    override fun show(fragment: Fragment, adUnit: AdUnit, callback: AdShowCallback?) {
        val activity = fragment.activity
        if (activity == null) {
            notifyShowFailed(adUnit, callback, ERROR_INVALID_HOST, "Fragment is not attached to an Activity")
            return
        }
        scope.launch { showInternal(activity, fragment, adUnit, callback) }
    }

    override fun loadAndShow(activity: Activity, adUnit: AdUnit, callback: AdShowCallback?) {
        scope.launch {
            loadAndShowInternal(activity, activity as? LifecycleOwner, adUnit, callback)
        }
    }

    override fun loadAndShow(fragment: Fragment, adUnit: AdUnit, callback: AdShowCallback?) {
        val activity = fragment.activity
        if (activity == null) {
            notifyShowFailed(adUnit, callback, ERROR_INVALID_HOST, "Fragment is not attached to an Activity")
            return
        }
        scope.launch { loadAndShowInternal(activity, fragment, adUnit, callback) }
    }

    override fun renderInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback?) {
        val provider = providerOf(adUnit) ?: run {
            val result = noProviderResult(adUnit)
            logLoadResult(adUnit, result)
            callback?.onResult(result)
            return
        }
        prepareAdUnit(provider, adUnit)
        AdLogger.event(adUnit, AdEventType.LOAD_STARTED)
        provider.renderInto(container, adUnit) { result ->
            logLoadResult(adUnit, result)
            (callback ?: NoopLoadCallback).onResult(result)
        }
    }

    override fun destroy(adUnit: AdUnit) {
        val registered = knownAdUnits.remove(PlacementKey.from(adUnit)) ?: adUnit
        providers[registered.providerName]?.destroy(registered)
        cache.cancel(
            registered,
            AdResult.Failure(registered.id, ERROR_DESTROYED, "Ad placement was destroyed")
        )
    }

    override fun destroyAll() {
        cache.clearAll { key ->
            AdResult.Failure(key.adName, ERROR_DESTROYED, "AdManager was destroyed")
        }
        providers.values.forEach(AdProvider::destroyAll)
        knownAdUnits.clear()
        showingFullScreenPlacement.set(null)
    }

    private fun showInternal(
        activity: Activity,
        lifecycleOwner: LifecycleOwner?,
        adUnit: AdUnit,
        callback: AdShowCallback?
    ) {
        if (!adUnit.type.supportsFullScreenShow()) {
            notifyShowFailed(adUnit, callback, ERROR_UNSUPPORTED_TYPE, "show only supports full-screen ads")
            return
        }
        if (!canShowFrom(activity, lifecycleOwner)) {
            notifyShowFailed(adUnit, callback, ERROR_INVALID_HOST, "Activity or Fragment is not RESUMED")
            return
        }
        if (showingFullScreenPlacement.get() != null) {
            notifyShowFailed(adUnit, callback, ERROR_AD_ALREADY_SHOWING, "Another full-screen ad is showing")
            return
        }

        val provider = providerOf(adUnit) ?: run {
            notifyShowFailed(adUnit, callback, ERROR_NO_PROVIDER, "Provider not registered")
            return
        }
        prepareAdUnit(provider, adUnit)
        if (provider.isReady(adUnit)) {
            performShow(activity, lifecycleOwner, provider, adUnit, callback)
            return
        }

        AdLogger.d("AdUnit '${adUnit.id}' is not ready; loading before show")
        preload(adUnit) { result ->
            when (result) {
                is AdResult.Success -> scope.launch {
                    performShow(activity, lifecycleOwner, provider, adUnit, callback)
                }
                is AdResult.Failure -> notifyShowFailed(adUnit, callback, result.errorCode, result.message)
                is AdResult.TimedOut -> notifyShowFailed(adUnit, callback, ERROR_TIMEOUT, "Ad load timed out")
            }
        }
    }

    private fun loadAndShowInternal(
        activity: Activity,
        lifecycleOwner: LifecycleOwner?,
        adUnit: AdUnit,
        callback: AdShowCallback?
    ) {
        if (!adUnit.type.supportsLoadAndShow()) {
            notifyShowFailed(
                adUnit,
                callback,
                ERROR_UNSUPPORTED_TYPE,
                "loadAndShow only supports interstitial and rewarded ads"
            )
            return
        }
        if (!canShowFrom(activity, lifecycleOwner)) {
            notifyShowFailed(adUnit, callback, ERROR_INVALID_HOST, "Activity or Fragment is not RESUMED")
            return
        }

        val provider = providerOf(adUnit) ?: run {
            notifyShowFailed(adUnit, callback, ERROR_NO_PROVIDER, "Provider not registered")
            return
        }
        prepareAdUnit(provider, adUnit)
        provider.destroy(adUnit)
        cache.cancel(
            adUnit,
            AdResult.Failure(adUnit.id, ERROR_REQUEST_SUPERSEDED, "Replaced by loadAndShow")
        )

        preload(adUnit) { result ->
            when (result) {
                is AdResult.Success -> scope.launch {
                    performShow(activity, lifecycleOwner, provider, adUnit, callback)
                }
                is AdResult.Failure -> notifyShowFailed(adUnit, callback, result.errorCode, result.message)
                is AdResult.TimedOut -> notifyShowFailed(adUnit, callback, ERROR_TIMEOUT, "Ad load timed out")
            }
        }
    }

    private fun performShow(
        activity: Activity,
        lifecycleOwner: LifecycleOwner?,
        provider: AdProvider,
        adUnit: AdUnit,
        callback: AdShowCallback?
    ) {
        if (!canShowFrom(activity, lifecycleOwner)) {
            notifyShowFailed(adUnit, callback, ERROR_INVALID_HOST, "Activity or Fragment is not RESUMED")
            return
        }
        if (!provider.isReady(adUnit)) {
            notifyShowFailed(adUnit, callback, ERROR_NOT_READY, "Ad '${adUnit.id}' is not ready")
            return
        }

        val placement = PlacementKey.from(adUnit)
        if (!showingFullScreenPlacement.compareAndSet(null, placement)) {
            notifyShowFailed(adUnit, callback, ERROR_AD_ALREADY_SHOWING, "Another full-screen ad is showing")
            return
        }

        val delegate = callback ?: NoopShowCallback
        val completed = AtomicBoolean(false)
        val guardedCallback = object : AdShowCallback {
            override fun onAdShown() {
                AdLogger.event(adUnit, AdEventType.SHOWN)
                delegate.onAdShown()
            }

            override fun onAdImpression() {
                AdLogger.event(adUnit, AdEventType.IMPRESSION)
                delegate.onAdImpression()
            }

            override fun onAdClicked() {
                AdLogger.event(adUnit, AdEventType.CLICKED)
                delegate.onAdClicked()
            }

            override fun onPaidEvent(revenue: AdRevenue) {
                AdLogger.event(adUnit, AdEventType.PAID, revenue = revenue)
                delegate.onPaidEvent(revenue)
            }

            override fun onUserEarnedReward(amount: Int, type: String) {
                AdLogger.event(
                    adUnit,
                    AdEventType.REWARD_EARNED,
                    rewardAmount = amount,
                    rewardType = type
                )
                delegate.onUserEarnedReward(amount, type)
            }

            override fun onAdDismissed() {
                if (!completed.compareAndSet(false, true)) return
                showingFullScreenPlacement.compareAndSet(placement, null)
                AdLogger.event(adUnit, AdEventType.DISMISSED)
                try {
                    delegate.onAdDismissed()
                } finally {
                    replenishAfterConsumption(adUnit)
                }
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                if (!completed.compareAndSet(false, true)) return
                showingFullScreenPlacement.compareAndSet(placement, null)
                AdLogger.event(
                    adUnit,
                    AdEventType.SHOW_FAILED,
                    errorCode = errorCode,
                    message = message
                )
                try {
                    delegate.onAdFailedToShow(errorCode, message)
                } finally {
                    replenishAfterConsumption(adUnit)
                }
            }
        }

        try {
            AdLogger.event(adUnit, AdEventType.SHOW_STARTED)
            provider.show(activity, adUnit, guardedCallback)
        } catch (throwable: Throwable) {
            guardedCallback.onAdFailedToShow(
                ERROR_SHOW_EXCEPTION,
                throwable.message ?: "Provider threw while showing the ad"
            )
        }
    }

    private fun replenishAfterConsumption(adUnit: AdUnit) {
        if (adUnit.cacheEnabled) preload(adUnit)
    }

    private fun canShowFrom(activity: Activity, lifecycleOwner: LifecycleOwner?): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        return lifecycleOwner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) ?: true
    }

    private fun prepareAdUnit(provider: AdProvider, adUnit: AdUnit) {
        val placement = PlacementKey.from(adUnit)
        var previous: AdUnit? = null
        knownAdUnits.compute(placement) { _, current ->
            previous = current
            adUnit
        }
        val oldUnit = previous ?: return
        if (AdKey.from(oldUnit) == AdKey.from(adUnit)) return

        cache.cancel(
            oldUnit,
            AdResult.Failure(oldUnit.id, ERROR_CONFIG_CHANGED, "Ad configuration changed")
        )
        provider.destroy(oldUnit)
        AdLogger.d("AdUnit '${adUnit.id}' network id changed; stale cache invalidated")
    }

    private suspend fun awaitLoad(provider: AdProvider, adUnit: AdUnit): AdResult =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            provider.load(appContext, adUnit) { result ->
                if (continuation.isActive) continuation.resumeWith(Result.success(result))
            }
        }

    private fun providerOf(adUnit: AdUnit): AdProvider? {
        val provider = providers[adUnit.providerName]
        if (provider == null) {
            AdLogger.e("Provider '${adUnit.providerName}' is not registered for '${adUnit.id}'")
        }
        return provider
    }

    private fun noProviderResult(adUnit: AdUnit) = AdResult.Failure(
        adUnit.id,
        ERROR_NO_PROVIDER,
        "Provider not registered"
    )

    private fun logLoadResult(adUnit: AdUnit, result: AdResult) {
        when (result) {
            is AdResult.Success -> AdLogger.event(adUnit, AdEventType.LOADED)
            is AdResult.Failure -> AdLogger.event(
                adUnit,
                AdEventType.LOAD_FAILED,
                errorCode = result.errorCode,
                message = result.message
            )
            is AdResult.TimedOut -> AdLogger.event(adUnit, AdEventType.LOAD_TIMED_OUT)
        }
    }

    private fun notifyShowFailed(
        adUnit: AdUnit,
        callback: AdShowCallback?,
        errorCode: Int,
        message: String
    ) {
        AdLogger.event(
            adUnit,
            AdEventType.SHOW_FAILED,
            errorCode = errorCode,
            message = message
        )
        callback?.onAdFailedToShow(errorCode, message)
    }

    private fun AdType.supportsLoadAndShow(): Boolean =
        this == AdType.INTERSTITIAL ||
            this == AdType.REWARDED ||
            this == AdType.REWARDED_INTERSTITIAL

    private fun AdType.supportsFullScreenShow(): Boolean =
        supportsLoadAndShow() || this == AdType.APP_OPEN

    private companion object {
        const val ERROR_NO_PROVIDER = -1
        const val ERROR_TIMEOUT = -2
        const val ERROR_UNSUPPORTED_TYPE = -3
        const val ERROR_NOT_READY = -4
        const val ERROR_INVALID_HOST = -5
        const val ERROR_AD_ALREADY_SHOWING = -6
        const val ERROR_REQUEST_SUPERSEDED = -7
        const val ERROR_CONFIG_CHANGED = -8
        const val ERROR_SHOW_EXCEPTION = -9
        const val ERROR_DESTROYED = -10

        val NoopLoadCallback = AdLoadCallback { }
        val NoopShowCallback = object : AdShowCallback {}
    }
}
