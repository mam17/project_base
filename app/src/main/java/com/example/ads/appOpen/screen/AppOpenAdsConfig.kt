package com.example.ads.appOpen.screen

import android.app.Activity
import android.content.Context
import com.example.ads.appOpen.screen.callbacks.AppOpenOnLoadCallBack
import com.example.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.example.ads.appOpen.screen.enums.AppOpenAdKey
import com.example.ads.appOpen.screen.manager.AppOpenManager
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.example.ads.utilities.InternetManager
import com.example.ads.utilities.RevenueTracker
import com.example.ads.utilities.MMPTracker


class AppOpenAdsConfig(
    private val context: Context,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : AppOpenManager() {

    fun loadAppOpenAd(adType: AppOpenAdKey, listener: AppOpenOnLoadCallBack? = null) {
        var interAdId = ""
        var isRemoteEnable = false

        when (adType) {
            AppOpenAdKey.SPLASH -> {
                interAdId = BuildConfig.appopen_resume
                isRemoteEnable = sharedPreferenceUtils.rcAppOpenSplash != 0
            }
        }

        val wrappedListener = object : AppOpenOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean, errorMessage: String?) {
                if (successfullyLoaded) {
                    // TODO: Replace with actual revenue from OnPaidEventListener (AdMob's AdValue.valueMicros / 1_000_000.0)
                    MMPTracker.trackAdRevenue(revenue = 0.0, adNetwork = "admob")
                    RevenueTracker.trackAdImpression(revenue = 0.0, source = "app_open", adNetwork = "admob")
                }
                listener?.onResponse(successfullyLoaded)
            }
        }

        loadAppOpen(
            context = context,
            adType = adType.value,
            appOpenId = interAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = wrappedListener
        )
    }

    fun showAppOpenAd(
        activity: Activity?,
        adType: AppOpenAdKey,
        listener: AppOpenOnShowCallBack? = null
    ) {
        showAppOpen(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            listener
        )
    }
}