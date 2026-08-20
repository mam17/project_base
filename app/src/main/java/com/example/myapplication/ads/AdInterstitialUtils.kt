package com.example.myapplication.ads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.utils.firebase.FirebaseConfigManager
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdInterstitialUtils {

    fun preload(adUnit: AdUnit = AdUnits.mainInterstitial) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadTwoFloor(placementName: String) {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.INTERSTITIAL)
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
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        AdFullScreenController.show(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    onDismissed()
                    handleNext()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    onFailed(message)
                    handleNext()
                }
            }
        )
    }

    fun show(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        AdFullScreenController.show(
            fragment = fragment,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    onDismissed()
                    handleNext()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    onFailed(message)
                    handleNext()
                }
            }
        )
    }

    fun showInterstitial(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        show(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    fun showInterstitial(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        show(
            fragment = fragment,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    fun showTwoFloor(
        activity: FragmentActivity,
        placementName: String,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.INTERSTITIAL)
        showTwoFloor(
            activity = activity,
            twoFloorUnits = units,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    fun showTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        AdFullScreenController.showTwoFloor(
            activity = activity,
            twoFloorUnits = twoFloorUnits,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    onDismissed()
                    handleNext()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    onFailed(message)
                    handleNext()
                }
            }
        )
    }

    fun showTwoFloor(
        fragment: Fragment,
        placementName: String,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.INTERSTITIAL)
        showTwoFloor(
            fragment = fragment,
            twoFloorUnits = units,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            onDismissed = onDismissed,
            onFailed = onFailed,
            action = action
        )
    }

    fun showTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        AdFullScreenController.showTwoFloor(
            fragment = fragment,
            twoFloorUnits = twoFloorUnits,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    onDismissed()
                    handleNext()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    onFailed(message)
                    handleNext()
                }
            }
        )
    }

    fun loadAndShow(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        AdFullScreenController.loadAndShow(
            activity = activity,
            adUnit = adUnit,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    onDismissed()
                    handleNext()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    onFailed(message)
                    handleNext()
                }
            }
        )
    }

    fun loadAndShow(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {},
        action: (() -> Unit)? = null
    ) {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }
        AdFullScreenController.loadAndShow(
            fragment = fragment,
            adUnit = adUnit,
            callback = object : AdShowCallback {
                override fun onAdDismissed() {
                    onDismissed()
                    handleNext()
                }

                override fun onAdFailedToShow(errorCode: Int, message: String) {
                    onFailed(message)
                    handleNext()
                }
            }
        )
    }
}
