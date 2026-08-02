package com.example.myapplication.ads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.AdUnit
import com.libads.core.callback.AdShowCallback

object AdInterstitialUtils {

    fun preload(adUnit: AdUnit = AdUnits.mainInterstitial) {
        AdManager.getInstance().preload(adUnit)
    }

    fun show(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        AdFullScreenController.show(
            activity = activity,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showCallback(adUnit, onDismissed, onFailed)
        )
    }

    fun show(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        showLoadingWhenNotReady: Boolean = true,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        AdFullScreenController.show(
            fragment = fragment,
            adUnit = adUnit,
            showLoadingWhenNotReady = showLoadingWhenNotReady,
            callback = showCallback(adUnit, onDismissed, onFailed)
        )
    }

    fun loadAndShow(
        activity: FragmentActivity,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        AdFullScreenController.loadAndShow(
            activity = activity,
            adUnit = adUnit,
            callback = loadAndShowCallback(onDismissed, onFailed)
        )
    }

    fun loadAndShow(
        fragment: Fragment,
        adUnit: AdUnit = AdUnits.mainInterstitial,
        onDismissed: () -> Unit = {},
        onFailed: (message: String) -> Unit = {}
    ) {
        AdFullScreenController.loadAndShow(
            fragment = fragment,
            adUnit = adUnit,
            callback = loadAndShowCallback(onDismissed, onFailed)
        )
    }

    private fun showCallback(
        adUnit: AdUnit,
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit
    ) = object : AdShowCallback {
        override fun onAdDismissed() {
            AdManager.getInstance().preload(adUnit)
            onDismissed()
        }

        override fun onAdFailedToShow(errorCode: Int, message: String) {
            AdManager.getInstance().preload(adUnit)
            onFailed(message)
        }
    }

    private fun loadAndShowCallback(
        onDismissed: () -> Unit,
        onFailed: (message: String) -> Unit
    ) = object : AdShowCallback {
        override fun onAdDismissed() = onDismissed()

        override fun onAdFailedToShow(errorCode: Int, message: String) {
            onFailed(message)
        }
    }
}
