package com.example.myapplication.ads

import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback

object AdMobAds {
    private const val TAG = "AdMobAds"
    private var isShowingAppOpen = false
    private var isLoadingAppOpen = false

    fun showBanner(
        container: ViewGroup,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val adUnit = AdUnits.mainBanner.copy(
            collapsiblePositionType = collapsiblePositionType
        )
        AdManager.getInstance().renderInto(container, adUnit, callback)
    }

    fun showNative(
        container: ViewGroup,
        callback: AdLoadCallback? = null
    ) {
        AdManager.getInstance().renderInto(container, AdUnits.mainNative, callback)
    }

    fun preloadAppOpenResume() {
        if (isLoadingAppOpen || AdManager.getInstance().isReady(AdUnits.appOpenResume)) return

        isLoadingAppOpen = true
        Log.d(TAG, "preloadAppOpenResume: start")
        AdManager.getInstance().preload(AdUnits.appOpenResume) { result ->
            isLoadingAppOpen = false
            when (result) {
                is AdResult.Success -> Log.d(TAG, "preloadAppOpenResume: loaded")
                is AdResult.Failure -> Log.e(TAG, "preloadAppOpenResume: failed ${result.errorCode} ${result.message}")
                is AdResult.TimedOut -> Log.w(TAG, "preloadAppOpenResume: timed out")
            }
        }
    }

    fun showAppOpenResume(activity: FragmentActivity) {
        if (isShowingAppOpen) return

        if (!AdManager.getInstance().isReady(AdUnits.appOpenResume)) {
            Log.d(TAG, "showAppOpenResume: not ready")
            preloadAppOpenResume()
            return
        }

        isShowingAppOpen = true
        Log.d(TAG, "showAppOpenResume: show")
        AdManager.getInstance().show(activity, AdUnits.appOpenResume, object : AdShowCallback {
            override fun onAdShown() {
                Log.d(TAG, "showAppOpenResume: shown")
            }

            override fun onAdDismissed() {
                Log.d(TAG, "showAppOpenResume: dismissed")
                isShowingAppOpen = false
                preloadAppOpenResume()
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                Log.e(TAG, "showAppOpenResume: failed $errorCode $message")
                isShowingAppOpen = false
                preloadAppOpenResume()
            }
        })
    }
}
