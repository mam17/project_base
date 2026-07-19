package com.example.myapplication.ads

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.ui.dialog.DialogLoadingAds
import com.libads.core.AdManager
import com.libads.core.AdUnit
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback

object FullScreenAdUtils {
    fun showInterstitial(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        showFullScreenAd(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    AdManager.getInstance().preload(adUnit)
                    onDismissed()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    AdManager.getInstance().preload(adUnit)
                    onFailed(message)
                }
            }
        )
    }

    fun showRewarded(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainRewarded,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        showFullScreenAd(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = object : AdShowCallback {
                override fun onUserEarnedReward(amount: Int, type: String) {
                    onRewardEarned(amount, type)
                }

                override fun onAdDismissed() {
                    AdManager.getInstance().preload(adUnit)
                    onDismissed()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    AdManager.getInstance().preload(adUnit)
                    onFailed(message)
                }
            }
        )
    }

    private fun showFullScreenAd(
        activity: FragmentActivity,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean,
        callback: AdShowCallback
    ) {
        val adManager = AdManager.getInstance()
        if (adManager.isReady(adUnit)) {
            adManager.show(activity, adUnit, callback)
            return
        }

        val loadingDialog = if (showLoadingWhenNotReady) DialogLoadingAds(activity) else null
        var isHandled = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (isHandled) return@Runnable
            isHandled = true
            loadingDialog?.dismiss()
            callback.onAdFailedToShow(ERROR_TIMEOUT, "Ad load timed out")
        }

        loadingDialog?.show()
        timeoutHandler.postDelayed(timeoutRunnable, adUnit.timeoutMillis)
        adManager.preload(adUnit) { result ->
            if (isHandled) return@preload
            isHandled = true
            timeoutHandler.removeCallbacks(timeoutRunnable)
            loadingDialog?.dismiss()
            when (result) {
                is AdResult.Success -> adManager.show(activity, adUnit, callback)
                is AdResult.Failure -> callback.onAdFailedToShow(result.errorCode, result.message)
                is AdResult.TimedOut -> callback.onAdFailedToShow(ERROR_TIMEOUT, "Ad load timed out")
            }
        }
    }

    private const val ERROR_TIMEOUT = -2
}
