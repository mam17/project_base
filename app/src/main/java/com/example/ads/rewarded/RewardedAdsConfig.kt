package com.example.ads.rewarded

import android.app.Activity
import android.content.Context
import com.example.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.example.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.example.ads.rewarded.enums.RewardedAdKey
import com.example.ads.rewarded.managers.RewardedManager
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.example.ads.utilities.InternetManager
import com.example.ads.utilities.LoadingDialogHelper
import com.example.ads.ui.dialog.DialogLoadingAds
import com.example.ads.utilities.extensions.trackMmpAdRevenue
import com.example.ads.utilities.extensions.trackMmpPurchase
import com.example.ads.utilities.RevenueTracker



class RewardedAdsConfig(
    private val context: Context?,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : RewardedManager() {

    fun loadRewardedAd(adType: RewardedAdKey, listener: RewardedOnLoadCallBack? = null) {
        loadRewardedAd(null, adType, listener)
    }

    fun loadRewardedAd(
        activity: Activity?,
        adType: RewardedAdKey,
        listener: RewardedOnLoadCallBack? = null
    ) {
        val isAppPurchased = sharedPreferenceUtils.isAppPurchased
        val isInternetConnected = internetManager.isInternetConnected

        val loadingDialog = if (activity != null) {
            DialogLoadingAds(activity).apply { show() }
        } else {
            null
        }

        val wrappedListener = LoadingDialogHelper.wrapRewardedCallback(loadingDialog, object : RewardedOnLoadCallBack {
            override fun onResponse(isSuccess: Boolean) {
                if (isSuccess) {
                    // TODO: Replace with actual revenue from OnPaidEventListener (AdMob's AdValue.valueMicros / 1_000_000.0)
                    if (activity != null) {
                        trackMmpAdRevenue(revenue = 0.0, adNetwork = "admob")
                    }
                    RevenueTracker.trackAdImpression(revenue = 0.0, source = "rewarded", adNetwork = "admob")
                }
                listener?.onResponse(isSuccess)
            }
        })

        when (adType) {
            RewardedAdKey.AI_FEATURE -> {
                loadRewardedWithFallback(
                    context = context,
                    adType = adType.value,
                    primaryId = BuildConfig.reward_feature_2f,
                    fallbackId = BuildConfig.reward_feature,
                    adEnable = sharedPreferenceUtils.rcRewardedAiFeature != 0,
                    isAppPurchased = isAppPurchased,
                    isInternetConnected = isInternetConnected,
                    listener = wrappedListener
                )
            }
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        adType: RewardedAdKey,
        listener: RewardedOnShowCallBack? = null
    ) {
        val wrappedListener = if (activity != null) {
            object : RewardedOnShowCallBack {
                override fun onAdDismissedFullScreenContent() {
                    listener?.onAdDismissedFullScreenContent()
                }

                override fun onAdFailedToShow() {
                    listener?.onAdFailedToShow()
                }

                override fun onAdShowedFullScreenContent() {
                    listener?.onAdShowedFullScreenContent()
                }

                override fun onAdImpression() {
                    listener?.onAdImpression()
                }

                override fun onUserEarnedReward() {
                    // TODO: Replace with actual reward value (set based on your business logic, e.g., in-app currency value)
                    trackMmpPurchase(revenue = 0.0, productId = "reward_${adType.value}")
                    RevenueTracker.trackPurchase(revenue = 0.0, productId = "reward_${adType.value}", quantity = 1)
                    listener?.onUserEarnedReward()
                }
            }
        } else {
            listener
        }

        showRewarded(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            wrappedListener
        )
    }
}