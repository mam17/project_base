package com.example.ads.appOpen.screen

import android.app.Activity
import android.content.Context
import com.example.ads.appOpen.screen.callbacks.AppOpenOnLoadCallBack
import com.example.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.example.ads.appOpen.screen.enums.AppOpenAdKey
import com.example.ads.appOpen.screen.manager.AppOpenManager
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.hypersoft.admobads.utilities.manager.InternetManager

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

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

        loadAppOpen(
            context = context,
            adType = adType.value,
            appOpenId = interAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = listener
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