package com.example.ads.rewarded

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.example.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.example.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.example.ads.rewarded.enums.RewardedAdKey
import com.example.ads.rewarded.managers.RewardedManager
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.hypersoft.admobads.utilities.manager.InternetManager

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */


/**
 * @param context: Can be of application class
 */
class RewardedAdsConfig(
    private val context: Context?,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : RewardedManager() {

    fun loadRewardedAd(adType: RewardedAdKey, listener: RewardedOnLoadCallBack? = null) {
        val isAppPurchased = sharedPreferenceUtils.isAppPurchased
        val isInternetConnected = internetManager.isInternetConnected

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
                    listener = listener
                )
            }
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        adType: RewardedAdKey,
        listener: RewardedOnShowCallBack? = null
    ) {
        showRewarded(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            listener
        )
    }

    private fun getResString(@StringRes resId: Int): String {
        return context?.resources?.getString(resId) ?: ""
    }
}