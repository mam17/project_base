package com.example.ads.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.example.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.example.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.example.ads.interstitial.enums.InterAdKey
import com.example.ads.interstitial.manager.InterstitialManager
import com.example.ads.utilities.Constants.TAG_ADS
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.example.ads.utilities.InternetManager
import com.example.ads.utilities.LoadingDialogHelper
import com.example.ads.ui.dialog.DialogLoadingAds
import com.example.ads.utilities.extensions.trackMmpAdRevenue
import com.example.ads.utilities.RevenueTracker


class InterstitialAdsConfig(
    private val context: Context?,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : InterstitialManager() {

    private val counterMap by lazy { HashMap<String, Int>() }

    fun loadInterstitialAd(adType: InterAdKey, listener: InterstitialOnLoadCallBack? = null) {
        loadInterstitialAd(null, adType, listener)
    }

    fun loadInterstitialAd(
        activity: Activity?,
        adType: InterAdKey,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        val isAppPurchased = sharedPreferenceUtils.isAppPurchased
        val isInternetConnected = internetManager.isInternetConnected

        val loadingDialog = if (activity != null) {
            DialogLoadingAds(activity).apply { show() }
        } else {
            null
        }

        val wrappedListener = LoadingDialogHelper.wrapInterstitialCallback(loadingDialog, object : InterstitialOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean) {
                if (successfullyLoaded) {
                    // TODO: Replace with actual revenue from OnPaidEventListener (AdMob's AdValue.valueMicros / 1_000_000.0)
                    if (activity != null) {
                       trackMmpAdRevenue(revenue = 0.0, adNetwork = "admob")
                    }
                    RevenueTracker.trackAdImpression(revenue = 0.0, source = "interstitial", adNetwork = "admob")
                }
                listener?.onResponse(successfullyLoaded)
            }
        })

        when (adType) {
            InterAdKey.INTER_HOME -> {
                loadInterstitialWithFallback(
                    context = context,
                    adType = adType.value,
                    primaryId = BuildConfig.inter_home_2f,
                    fallbackId = BuildConfig.inter_home,
                    adEnable = sharedPreferenceUtils.rcInterHome != 0,
                    isAppPurchased = isAppPurchased,
                    isInternetConnected = isInternetConnected,
                    listener = wrappedListener
                )
            }

            InterAdKey.FEATURE -> {
                loadInterstitial(
                    context = context,
                    adType = adType.value,
                    interId = BuildConfig.inter_home,
                    adEnable = sharedPreferenceUtils.rcInterFeature != 0,
                    isAppPurchased = isAppPurchased,
                    isInternetConnected = isInternetConnected,
                    listener = wrappedListener
                )
            }
        }
    }

    fun showInterstitialAd(
        activity: Activity?,
        adType: InterAdKey,
        listener: InterstitialOnShowCallBack? = null
    ) {
        showInterstitial(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            listener
        )
    }

    /**
     * @param adType   Key of the Ad, it should be unique id and should be case-sensitive
     * @param remoteCounter   Pass remote counter value, if the value is n, it will load on "n-1". In case of <= 2, it will load everytime
     * @param loadOnStart  Determine whether ad should be load on the very first time or not?
     *
     *  e.g. remoteCounter = 3, ad will  load on "n-1" = 2
     *      if (loadOnStart) {
     *          // 1, 0, 0, 1, 0, 0, 1, 0, 0 ... so on
     *      } else {
     *          // 0, 0, 1, 0, 0, 1, 0, 0, 1 ... so on
     *      }
     */

    fun loadInterstitialAd(
        adType: InterAdKey,
        remoteCounter: Int,
        loadOnStart: Boolean,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        when (loadOnStart) {
            true -> counterMap.putIfAbsent(adType.value, remoteCounter - 1)
            false -> counterMap.putIfAbsent(adType.value, 0)
        }

        if (counterMap.containsKey(adType.value)) {
            val counter = counterMap[adType.value] ?: 0
            counterMap[adType.value] = counter + 1
            counterMap[adType.value]?.let { currentCounter ->
                Log.d(
                    TAG_ADS,
                    "$adType -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: $currentCounter"
                )
                if (currentCounter >= remoteCounter - 1) {
                    counterMap[adType.value] = 0
                    loadInterstitialAd(adType = adType, listener = listener)
                }
            }
        }
    }

}