package com.example.myapplication.ads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdRewardUtils {

    // ==========================================
    // Rewarded Ads
    // ==========================================

    fun preloadRewarded(placementName: String = AdUnits.REWARD_FEATURE) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED) ?: AdUnits.mainRewarded
        preloadRewarded(unit)
    }

    fun preloadRewarded(adUnit: AdUnit) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadRewardedTwoFloor(placementName: String = AdUnits.REWARD_FEATURE) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED)
        if (units.unit2f != null) {
            AdManager.getInstance().preload(units.unit2f) { result ->
                if (result !is com.libads.core.callback.AdResult.Success && units.unitBase != null) {
                    AdManager.getInstance().preload(units.unitBase)
                }
            }
        } else if (units.unitBase != null) {
            AdManager.getInstance().preload(units.unitBase)
        }
    }

    fun showRewarded(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED) ?: AdUnits.mainRewarded
        showRewarded(activity, unit, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewarded(
        activity: FragmentActivity,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.show(activity, adUnit, showLoadingWhenNotReady, callback)
    }

    fun showRewarded(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED) ?: AdUnits.mainRewarded
        showRewarded(fragment, unit, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewarded(
        fragment: Fragment,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.show(fragment, adUnit, showLoadingWhenNotReady, callback)
    }

    fun showRewardedTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED)
        showRewardedTwoFloor(activity, units, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewardedTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.showTwoFloor(activity, twoFloorUnits, showLoadingWhenNotReady, callback)
    }

    fun showRewardedTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED)
        showRewardedTwoFloor(fragment, units, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewardedTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.showTwoFloor(fragment, twoFloorUnits, showLoadingWhenNotReady, callback)
    }

    fun loadAndShowRewarded(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED) ?: AdUnits.mainRewarded
        loadAndShowRewarded(activity, unit, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewarded(
        activity: FragmentActivity,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShow(activity, adUnit, callback)
    }

    fun loadAndShowRewarded(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED) ?: AdUnits.mainRewarded
        loadAndShowRewarded(fragment, unit, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewarded(
        fragment: Fragment,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShow(fragment, adUnit, callback)
    }

    fun loadAndShowRewardedTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED)
        loadAndShowRewardedTwoFloor(activity, units, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewardedTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShowTwoFloor(activity, twoFloorUnits, callback)
    }

    fun loadAndShowRewardedTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED)
        loadAndShowRewardedTwoFloor(fragment, units, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewardedTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShowTwoFloor(fragment, twoFloorUnits, callback)
    }

    // ==========================================
    // Rewarded Interstitial Ads
    // ==========================================

    fun preloadRewardedInterstitial(placementName: String = AdUnits.REWARD_FEATURE) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED_INTERSTITIAL) ?: AdUnits.mainRewardedInterstitial
        preloadRewardedInterstitial(unit)
    }

    fun preloadRewardedInterstitial(adUnit: AdUnit) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadRewardedInterstitialTwoFloor(placementName: String = AdUnits.REWARD_FEATURE) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED_INTERSTITIAL)
        if (units.unit2f != null) {
            AdManager.getInstance().preload(units.unit2f) { result ->
                if (result !is com.libads.core.callback.AdResult.Success && units.unitBase != null) {
                    AdManager.getInstance().preload(units.unitBase)
                }
            }
        } else if (units.unitBase != null) {
            AdManager.getInstance().preload(units.unitBase)
        }
    }

    fun showRewardedInterstitial(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED_INTERSTITIAL) ?: AdUnits.mainRewardedInterstitial
        showRewardedInterstitial(activity, unit, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewardedInterstitial(
        activity: FragmentActivity,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.show(activity, adUnit, showLoadingWhenNotReady, callback)
    }

    fun showRewardedInterstitial(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED_INTERSTITIAL) ?: AdUnits.mainRewardedInterstitial
        showRewardedInterstitial(fragment, unit, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewardedInterstitial(
        fragment: Fragment,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.show(fragment, adUnit, showLoadingWhenNotReady, callback)
    }

    fun showRewardedInterstitialTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED_INTERSTITIAL)
        showRewardedInterstitialTwoFloor(activity, units, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewardedInterstitialTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.showTwoFloor(activity, twoFloorUnits, showLoadingWhenNotReady, callback)
    }

    fun showRewardedInterstitialTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED_INTERSTITIAL)
        showRewardedInterstitialTwoFloor(fragment, units, showLoadingWhenNotReady, onRewardEarned, onDismissed, onFailed, action)
    }

    fun showRewardedInterstitialTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.showTwoFloor(fragment, twoFloorUnits, showLoadingWhenNotReady, callback)
    }

    fun loadAndShowRewardedInterstitial(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED_INTERSTITIAL) ?: AdUnits.mainRewardedInterstitial
        loadAndShowRewardedInterstitial(activity, unit, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewardedInterstitial(
        activity: FragmentActivity,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShow(activity, adUnit, callback)
    }

    fun loadAndShowRewardedInterstitial(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.REWARDED_INTERSTITIAL) ?: AdUnits.mainRewardedInterstitial
        loadAndShowRewardedInterstitial(fragment, unit, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewardedInterstitial(
        fragment: Fragment,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShow(fragment, adUnit, callback)
    }

    fun loadAndShowRewardedInterstitialTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED_INTERSTITIAL)
        loadAndShowRewardedInterstitialTwoFloor(activity, units, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewardedInterstitialTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShowTwoFloor(activity, twoFloorUnits, callback)
    }

    fun loadAndShowRewardedInterstitialTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.REWARD_FEATURE,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.REWARDED_INTERSTITIAL)
        loadAndShowRewardedInterstitialTwoFloor(fragment, units, onRewardEarned, onDismissed, onFailed, action)
    }

    fun loadAndShowRewardedInterstitialTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        AdFullScreenController.loadAndShowTwoFloor(fragment, twoFloorUnits, callback)
    }

    private fun createRewardCallback(
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit,
        action: (() -> Unit)?
    ): AdShowCallback {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        return object : AdShowCallback {
            override fun onUserEarnedReward(amount: Int, type: String) {
                onRewardEarned(amount, type)
            }

            override fun onAdDismissed() {
                onDismissed()
                handleNext()
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                onFailed(message)
                handleNext()
            }
        }
    }
}
