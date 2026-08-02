package com.example.myapplication.ads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.AdUnit
import com.libads.core.callback.AdShowCallback

object AdRewardUtils {

    fun preloadRewarded(adUnit: AdUnit = AdUnits.mainRewarded) {
        AdManager.getInstance().preload(adUnit)
    }

    fun showRewarded(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainRewarded,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        AdFullScreenController.show(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showRewardCallback(adUnit, onRewardEarned, onDismissed, onFailed)
        )
    }

    fun showRewarded(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainRewarded,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        AdFullScreenController.show(
            fragment = fragment,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showRewardCallback(adUnit, onRewardEarned, onDismissed, onFailed)
        )
    }

    fun loadAndShowRewarded(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainRewarded,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        loadAndShowReward(
            activity = activity,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed
        )
    }

    fun loadAndShowRewarded(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainRewarded,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        loadAndShowReward(
            fragment = fragment,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed
        )
    }

    fun preloadRewardedInterstitial(adUnit: AdUnit = AdUnits.mainRewardedInterstitial) {
        AdManager.getInstance().preload(adUnit)
    }

    fun loadAndShowRewardedInterstitial(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainRewardedInterstitial,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        loadAndShowReward(
            activity = activity,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed
        )
    }

    fun loadAndShowRewardedInterstitial(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainRewardedInterstitial,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        loadAndShowReward(
            fragment = fragment,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed
        )
    }

    private fun loadAndShowReward(
        activity: FragmentActivity,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        AdFullScreenController.loadAndShow(
            activity = activity,
            adUnit = adUnit,
            callback = loadAndShowRewardCallback(onRewardEarned, onDismissed, onFailed)
        )
    }

    private fun loadAndShowReward(
        fragment: Fragment,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        AdFullScreenController.loadAndShow(
            fragment = fragment,
            adUnit = adUnit,
            callback = loadAndShowRewardCallback(onRewardEarned, onDismissed, onFailed)
        )
    }

    private fun showRewardCallback(
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit
    ) = object : AdShowCallback {
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

    private fun loadAndShowRewardCallback(
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit
    ) = object : AdShowCallback {
        override fun onUserEarnedReward(amount: Int, type: String) {
            onRewardEarned(amount, type)
        }

        override fun onAdDismissed() = onDismissed()

        override fun onAdFailedToShow(errorCode: Int, message: String) {
            onFailed(message)
        }
    }
}
