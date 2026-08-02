package com.example.myapplication.ads

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.ui.dialog.DialogLoadingAds
import com.libads.core.AdManager
import com.libads.core.AdUnit
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdRevenue
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

internal object AdFullScreenController {
    private const val ERROR_TIMEOUT = -2
    private const val ERROR_INVALID_HOST = -5

    fun show(
        activity: FragmentActivity,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean,
        callback: AdShowCallback
    ) {
        showInternal(
            activity = activity,
            lifecycleOwner = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = callback,
            showAction = { guarded ->
                AdManager.getInstance().show(activity, adUnit, guarded)
            }
        )
    }

    fun show(
        fragment: Fragment,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean,
        callback: AdShowCallback
    ) {
        val activity = fragment.activity
        if (activity == null) {
            callback.onAdFailedToShow(ERROR_INVALID_HOST, "Fragment is not attached")
            return
        }
        showInternal(
            activity = activity,
            lifecycleOwner = fragment.viewLifecycleOwnerOrFragment(),
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = callback,
            showAction = { guarded ->
                AdManager.getInstance().show(fragment, adUnit, guarded)
            }
        )
    }

    fun loadAndShow(
        activity: FragmentActivity,
        adUnit: AdUnit,
        callback: AdShowCallback
    ) {
        loadAndShowInternal(
            activity = activity,
            lifecycleOwner = activity,
            callback = callback,
            showAction = { guarded ->
                AdManager.getInstance().loadAndShow(activity, adUnit, guarded)
            }
        )
    }

    fun loadAndShow(
        fragment: Fragment,
        adUnit: AdUnit,
        callback: AdShowCallback
    ) {
        val activity = fragment.activity
        if (activity == null) {
            callback.onAdFailedToShow(ERROR_INVALID_HOST, "Fragment is not attached")
            return
        }
        loadAndShowInternal(
            activity = activity,
            lifecycleOwner = fragment.viewLifecycleOwnerOrFragment(),
            callback = callback,
            showAction = { guarded ->
                AdManager.getInstance().loadAndShow(fragment, adUnit, guarded)
            }
        )
    }

    private fun showInternal(
        activity: FragmentActivity,
        lifecycleOwner: LifecycleOwner,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean,
        callback: AdShowCallback,
        showAction: (AdShowCallback) -> Unit
    ) {
        val adManager = AdManager.getInstance()
        val isReady = adManager.isReady(adUnit)
        val loadingDialog = if (!isReady && showLoadingWhenNotReady) {
            DialogLoadingAds(activity)
        } else {
            null
        }
        val operation = LifecycleAdOperation(lifecycleOwner, loadingDialog, callback)
        if (!operation.isActive()) return

        if (isReady) {
            showAction(operation.callback)
            return
        }

        operation.showLoading()
        operation.scheduleTimeout(adUnit.timeoutMillis) {
            operation.callback.onAdFailedToShow(ERROR_TIMEOUT, "Ad load timed out")
        }
        adManager.preload(adUnit) { result ->
            if (!operation.resolveLoading()) return@preload
            when (result) {
                is AdResult.Success -> showAction(operation.callback)
                is AdResult.Failure -> operation.callback.onAdFailedToShow(
                    result.errorCode,
                    result.message
                )
                is AdResult.TimedOut -> operation.callback.onAdFailedToShow(
                    ERROR_TIMEOUT,
                    "Ad load timed out"
                )
            }
        }
    }

    private fun loadAndShowInternal(
        activity: FragmentActivity,
        lifecycleOwner: LifecycleOwner,
        callback: AdShowCallback,
        showAction: (AdShowCallback) -> Unit
    ) {
        val operation = LifecycleAdOperation(
            lifecycleOwner = lifecycleOwner,
            loadingDialog = DialogLoadingAds(activity),
            delegate = callback
        )
        if (!operation.isActive()) return
        operation.showLoading()
        showAction(operation.callback)
    }

    private fun Fragment.viewLifecycleOwnerOrFragment(): LifecycleOwner {
        return runCatching { viewLifecycleOwner }.getOrDefault(this)
    }

    private class LifecycleAdOperation(
        private val lifecycleOwner: LifecycleOwner,
        private val loadingDialog: DialogLoadingAds?,
        private val delegate: AdShowCallback
    ) : DefaultLifecycleObserver {
        private val mainHandler = Handler(Looper.getMainLooper())
        private val terminal = AtomicBoolean(false)
        private val loadingResolved = AtomicBoolean(false)
        private var timeoutRunnable: Runnable? = null

        val callback = object : AdShowCallback {
            override fun onAdShown() {
                if (!isActive()) return
                resolveLoading()
                delegate.onAdShown()
            }

            override fun onAdImpression() {
                if (isActive()) delegate.onAdImpression()
            }

            override fun onAdClicked() {
                if (isActive()) delegate.onAdClicked()
            }

            override fun onPaidEvent(revenue: AdRevenue) {
                if (isActive()) delegate.onPaidEvent(revenue)
            }

            override fun onUserEarnedReward(amount: Int, type: String) {
                if (isActive()) delegate.onUserEarnedReward(amount, type)
            }

            override fun onAdDismissed() {
                finish { delegate.onAdDismissed() }
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                finish { delegate.onAdFailedToShow(errorCode, message) }
            }
        }

        init {
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                terminal.set(true)
            } else {
                lifecycleOwner.lifecycle.addObserver(this)
            }
        }

        fun isActive(): Boolean = !terminal.get() &&
            lifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED

        fun showLoading() {
            if (!isActive()) return
            runCatching { loadingDialog?.show() }
        }

        fun scheduleTimeout(delayMillis: Long, onTimeout: () -> Unit) {
            if (!isActive()) return
            val runnable = Runnable {
                if (resolveLoading()) onTimeout()
            }
            timeoutRunnable = runnable
            mainHandler.postDelayed(runnable, delayMillis)
        }

        fun resolveLoading(): Boolean {
            if (!isActive() || !loadingResolved.compareAndSet(false, true)) return false
            cancelTimeout()
            dismissLoading()
            return true
        }

        override fun onDestroy(owner: LifecycleOwner) {
            if (terminal.compareAndSet(false, true)) cleanup()
        }

        private fun finish(action: () -> Unit) {
            if (!terminal.compareAndSet(false, true)) return
            cleanup()
            action()
        }

        private fun cleanup() {
            loadingResolved.set(true)
            cancelTimeout()
            dismissLoading()
            lifecycleOwner.lifecycle.removeObserver(this)
        }

        private fun cancelTimeout() {
            timeoutRunnable?.let(mainHandler::removeCallbacks)
            timeoutRunnable = null
        }

        private fun dismissLoading() {
            runCatching {
                if (loadingDialog?.isShowing == true) loadingDialog.dismiss()
            }
        }
    }
}
