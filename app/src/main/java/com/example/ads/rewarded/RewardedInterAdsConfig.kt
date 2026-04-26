package com.example.ads.rewarded

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import com.example.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.example.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.example.ads.rewarded.enums.RewardedInterAdKey
import com.example.ads.rewarded.managers.RewardedInterManager
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.ads.utilities.InternetManager
import com.example.ads.utilities.LoadingDialogHelper
import com.example.ads.ui.dialog.DialogLoadingAds

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
class RewardedInterAdsConfig(
    private val context: Context?,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager
) : RewardedInterManager() {

    fun loadRewardedInterAd(adType: RewardedInterAdKey, listener: RewardedOnLoadCallBack? = null) {
        loadRewardedInterAd(null, adType, listener)
    }

    fun loadRewardedInterAd(
        activity: Activity?,
        adType: RewardedInterAdKey,
        listener: RewardedOnLoadCallBack? = null
    ) {
        var rewardedInterAdId = ""
        var isRemoteEnable = false

        when (adType) {
            RewardedInterAdKey.AI_FEATURE -> {
                rewardedInterAdId = "getResString(R.string.admob_rewarded_inter_ai_feature_id)"
                isRemoteEnable = sharedPreferenceUtils.rcRewardedInterAiFeature != 0
            }
        }

        val loadingDialog = if (activity != null) {
            DialogLoadingAds(activity).apply { show() }
        } else {
            null
        }

        val wrappedListener = LoadingDialogHelper.wrapRewardedCallback(loadingDialog, listener)

        loadRewardedInter(
            context = context,
            adType = adType.value,
            rewardedInterId = rewardedInterAdId,
            adEnable = isRemoteEnable,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            isInternetConnected = internetManager.isInternetConnected,
            listener = wrappedListener
        )
    }

    fun showRewardedInterAd(
        activity: Activity?,
        adType: RewardedInterAdKey,
        listener: RewardedOnShowCallBack? = null
    ) {
        showRewardedInter(
            activity = activity,
            adType = adType.value,
            isAppPurchased = sharedPreferenceUtils.isAppPurchased,
            listener
        )
    }
}