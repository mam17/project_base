package com.example.myapplication.ads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdInterstitialUtils {

    fun preload(placementName: String = AdUnits.INTER_FEATURE_FIRST) {
        val unit = AdUnits.getUnit(placementName, AdType.INTERSTITIAL) ?: AdUnits.mainInterstitial
        preload(unit)
    }

    fun preload(adUnit: AdUnit) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadTwoFloor(placementName: String = AdUnits.INTER_FEATURE_FIRST) {
        val units = AdUnits.getTwoFloor(placementName, AdType.INTERSTITIAL)
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

    fun show(
        activity: FragmentActivity,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.INTERSTITIAL) ?: AdUnits.mainInterstitial
        show(activity, unit, showLoadingWhenNotReady, onDismissed, onFailed, action)
    }

    fun show(
        activity: FragmentActivity,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.show(activity, adUnit, showLoadingWhenNotReady, callback)
    }

    fun show(
        fragment: Fragment,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.INTERSTITIAL) ?: AdUnits.mainInterstitial
        show(fragment, unit, showLoadingWhenNotReady, onDismissed, onFailed, action)
    }

    fun show(
        fragment: Fragment,
        adUnit: AdUnit,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.show(fragment, adUnit, showLoadingWhenNotReady, callback)
    }

    fun showTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.INTERSTITIAL)
        showTwoFloor(activity, units, showLoadingWhenNotReady, onDismissed, onFailed, action)
    }

    fun showTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.showTwoFloor(activity, twoFloorUnits, showLoadingWhenNotReady, callback)
    }

    fun showTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.INTERSTITIAL)
        showTwoFloor(fragment, units, showLoadingWhenNotReady, onDismissed, onFailed, action)
    }

    fun showTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.showTwoFloor(fragment, twoFloorUnits, showLoadingWhenNotReady, callback)
    }

    fun loadAndShow(
        activity: FragmentActivity,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.INTERSTITIAL) ?: AdUnits.mainInterstitial
        loadAndShow(activity, unit, onDismissed, onFailed, action)
    }

    fun loadAndShow(
        activity: FragmentActivity,
        adUnit: AdUnit,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.loadAndShow(activity, adUnit, callback)
    }

    fun loadAndShow(
        fragment: Fragment,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.INTERSTITIAL) ?: AdUnits.mainInterstitial
        loadAndShow(fragment, unit, onDismissed, onFailed, action)
    }

    fun loadAndShow(
        fragment: Fragment,
        adUnit: AdUnit,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.loadAndShow(fragment, adUnit, callback)
    }

    fun loadAndShowTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.INTERSTITIAL)
        loadAndShowTwoFloor(activity, units, onDismissed, onFailed, action)
    }

    fun loadAndShowTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.loadAndShowTwoFloor(activity, twoFloorUnits, callback)
    }

    fun loadAndShowTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.INTER_FEATURE_FIRST,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.INTERSTITIAL)
        loadAndShowTwoFloor(fragment, units, onDismissed, onFailed, action)
    }

    fun loadAndShowTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val callback = createShowCallback(onDismissed, onFailed, action)
        AdFullScreenController.loadAndShowTwoFloor(fragment, twoFloorUnits, callback)
    }

    private fun createShowCallback(
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
