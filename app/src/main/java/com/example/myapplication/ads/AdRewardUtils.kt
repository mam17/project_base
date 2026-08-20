package com.example.myapplication.ads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.utils.firebase.FirebaseConfigManager
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdRewardUtils {

    fun preloadRewarded(adUnit: AdUnit = AdUnits.mainRewarded) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadRewardedTwoFloor(placementName: String = "reward_feature") {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.REWARDED)
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
        adUnit: AdUnit = AdUnits.mainRewarded,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        AdFullScreenController.show(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        )
    }

    fun showRewarded(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainRewarded,
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        AdFullScreenController.show(
            fragment = fragment,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        )
    }

    fun showRewardedTwoFloor(
        activity: FragmentActivity,
        placementName: String = "reward_feature",
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.REWARDED)
        showRewardedTwoFloor(
            activity = activity,
            twoFloorUnits = units,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
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
        AdFullScreenController.showTwoFloor(
            activity = activity,
            twoFloorUnits = twoFloorUnits,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        )
    }

    fun showRewardedTwoFloor(
        fragment: Fragment,
        placementName: String = "reward_feature",
        showLoadingWhenNotReady: Boolean = true,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.REWARDED)
        showRewardedTwoFloor(
            fragment = fragment,
            twoFloorUnits = units,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
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
        AdFullScreenController.showTwoFloor(
            fragment = fragment,
            twoFloorUnits = twoFloorUnits,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        )
    }

    fun loadAndShowRewarded(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainRewarded,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        loadAndShowReward(
            activity = activity,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    fun loadAndShowRewarded(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainRewarded,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        loadAndShowReward(
            fragment = fragment,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
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
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        loadAndShowReward(
            activity = activity,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    fun loadAndShowRewardedInterstitial(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainRewardedInterstitial,
        onRewardEarned: (amount: Int, type: String) -> Unit = { _, _ -> },
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        loadAndShowReward(
            fragment = fragment,
            adUnit = adUnit,
            onRewardEarned = onRewardEarned,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    private fun loadAndShowReward(
        activity: FragmentActivity,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit,
        action: (() -> Unit)?
    ) {
        AdFullScreenController.loadAndShow(
            activity = activity,
            adUnit = adUnit,
            callback = showRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        )
    }

    private fun loadAndShowReward(
        fragment: Fragment,
        adUnit: AdUnit,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit,
        action: (() -> Unit)?
    ) {
        AdFullScreenController.loadAndShow(
            fragment = fragment,
            adUnit = adUnit,
            callback = showRewardCallback(onRewardEarned, onDismissed, onFailed, action)
        )
    }

    private fun showRewardCallback(
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
