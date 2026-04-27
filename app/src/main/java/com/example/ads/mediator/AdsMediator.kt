package com.example.ads.mediator

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.ads.appOpen.screen.AppOpenAdsConfig
import com.example.ads.banner.presentation.viewModels.ViewModelBanner
import com.example.ads.interstitial.InterstitialAdsConfig
import com.example.ads.mediator.adapters.CallbackAdapters
import com.example.ads.mediator.callbacks.AdLoadCallback
import com.example.ads.mediator.callbacks.AdShowCallback
import com.example.ads.mediator.config.MediationConfig
import com.example.ads.natives.presentation.viewModels.ViewModelNative
import com.example.ads.rewarded.RewardedAdsConfig
import com.example.ads.rewarded.RewardedInterAdsConfig
import com.example.ads.ui.dialog.DialogLoadingAds
import com.example.ads.utilities.LoadingDialogHelper
import com.example.ads.utilities.SharedPreferenceUtils

/**
 * Unified facade for all ad operations (load, show) across all ad types.
 * Routes to appropriate config/manager based on AdKey type.
 * Simplifies API from 12 methods to 2 core methods (loadAd, showAd).
 */
class AdsMediator(
    private val context: Context,
    private val interstitialAdsConfig: InterstitialAdsConfig,
    private val rewardedAdsConfig: RewardedAdsConfig,
    private val rewardedInterAdsConfig: RewardedInterAdsConfig,
    private val appOpenAdsConfig: AppOpenAdsConfig,
    private val viewModelBanner: ViewModelBanner,
    private val viewModelNative: ViewModelNative,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val loadingDialogHelper: LoadingDialogHelper,
    private val mediationConfig: MediationConfig = MediationConfig()
) {

    private val loadingDialogs = mutableMapOf<String, DialogLoadingAds>()
    private val tag = mediationConfig.logTag

    /**
     * Load an ad with automatic type detection.
     * @param activity Optional activity for showing loading dialog
     * @param adKey Unified ad key (wraps native enum)
     * @param callback Unified load callback (optional)
     * @param showLoadingDialog Whether to show loading dialog (default from config)
     */
    fun loadAd(
        activity: Activity? = null,
        adKey: AdKey,
        callback: AdLoadCallback? = null,
        showLoadingDialog: Boolean = mediationConfig.showLoadingDialog
    ) {
        if (mediationConfig.enableLogging) {
            Log.d(tag, "loadAd: ${adKey::class.simpleName} with key '${adKey.value}'")
        }

        val dialog = if (activity != null && showLoadingDialog) {
            DialogLoadingAds(activity).apply { show() }
        } else {
            null
        }

        val wrappedCallback = wrapLoadCallback(callback, dialog, adKey.value)

        when (adKey) {
            is AdKey.Interstitial -> {
                interstitialAdsConfig.loadInterstitialAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toInterstitialLoadCallback(wrappedCallback)
                )
            }

            is AdKey.Rewarded -> {
                rewardedAdsConfig.loadRewardedAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toRewardedLoadCallback(wrappedCallback)
                )
            }

            is AdKey.RewardedInterstitial -> {
                rewardedInterAdsConfig.loadRewardedInterAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toRewardedLoadCallback(wrappedCallback)
                )
            }

            is AdKey.AppOpen -> {
                appOpenAdsConfig.loadAppOpenAd(
                    adKey.key,
                    CallbackAdapters.toAppOpenLoadCallback(wrappedCallback)
                )
            }

            is AdKey.Banner -> {
                if (dialog != null) {
                    loadingDialogs[adKey.value] = dialog
                }
                if (mediationConfig.enableLogging) {
                    Log.d(tag, "Banner loading via ViewModel (not yet implemented in mediator)")
                }
            }

            is AdKey.Native -> {
                if (dialog != null) {
                    loadingDialogs[adKey.value] = dialog
                }
                if (mediationConfig.enableLogging) {
                    Log.d(tag, "Native loading via ViewModel (not yet implemented in mediator)")
                }
            }
        }
    }

    /**
     * Show an ad with automatic type detection.
     * @param activity Activity required for showing full-screen ads
     * @param adKey Unified ad key (wraps native enum)
     * @param callback Unified show callback (optional)
     */
    fun showAd(
        activity: Activity,
        adKey: AdKey,
        callback: AdShowCallback? = null
    ) {
        if (mediationConfig.enableLogging) {
            Log.d(tag, "showAd: ${adKey::class.simpleName} with key '${adKey.value}'")
        }

        when (adKey) {
            is AdKey.Interstitial -> {
                interstitialAdsConfig.showInterstitialAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toInterstitialShowCallback(callback)
                )
            }

            is AdKey.Rewarded -> {
                rewardedAdsConfig.showRewardedAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toRewardedShowCallback(callback)
                )
            }

            is AdKey.RewardedInterstitial -> {
                rewardedInterAdsConfig.showRewardedInterAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toRewardedShowCallback(callback)
                )
            }

            is AdKey.AppOpen -> {
                appOpenAdsConfig.showAppOpenAd(
                    activity,
                    adKey.key,
                    CallbackAdapters.toAppOpenShowCallback(callback)
                )
            }

            is AdKey.Banner -> {
                if (mediationConfig.enableLogging) {
                    Log.d(tag, "Banner show via ViewModel (not applicable)")
                }
            }

            is AdKey.Native -> {
                if (mediationConfig.enableLogging) {
                    Log.d(tag, "Native show via ViewModel (not applicable)")
                }
            }
        }
    }

    /**
     * Check if an ad is loaded and ready to show.
     */
    fun isAdLoaded(adKey: AdKey): Boolean {
        return when (adKey) {
            is AdKey.Interstitial -> interstitialAdsConfig.isInterstitialLoaded()
            is AdKey.Rewarded -> rewardedAdsConfig.isRewardedLoaded()
            is AdKey.RewardedInterstitial -> rewardedInterAdsConfig.isRewardedInterLoaded()
            is AdKey.AppOpen -> appOpenAdsConfig.isAppOpenLoaded()
            else -> false
        }
    }

    /**
     * Get current mediation configuration.
     */
    fun getMediationConfig(): MediationConfig = mediationConfig

    /**
     * Get SharedPreference utility for ad settings.
     */
    fun getSharedPreferenceUtils(): SharedPreferenceUtils = sharedPreferenceUtils

    // ===== Private Helpers =====

    private fun wrapLoadCallback(
        callback: AdLoadCallback?,
        dialog: DialogLoadingAds?,
        adKey: String
    ): AdLoadCallback {
        return object : AdLoadCallback {
            override fun onSuccess() {
                dismissDialog(dialog, adKey)
                callback?.onSuccess()
            }

            override fun onFailure(errorMessage: String?) {
                dismissDialog(dialog, adKey)
                if (mediationConfig.enableLogging) {
                    Log.e(tag, "Ad load failed for $adKey: $errorMessage")
                }
                callback?.onFailure(errorMessage)
            }
        }
    }

    private fun dismissDialog(dialog: DialogLoadingAds?, adKey: String) {
        try {
            dialog?.dismiss()
            loadingDialogs.remove(adKey)
        } catch (e: Exception) {
            if (mediationConfig.enableLogging) {
                Log.w(tag, "Error dismissing loading dialog for $adKey", e)
            }
        }
    }
}
