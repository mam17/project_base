package com.example.ads.banner.domain.useCases

import android.content.Context
import android.util.Log
import com.example.ads.banner.data.entities.ItemBannerAd
import com.example.ads.banner.data.repositories.RepositoryBannerImpl
import com.example.ads.banner.presentation.enums.BannerAdKey
import com.example.ads.banner.presentation.enums.BannerAdType
import com.example.ads.utilities.Constants.TAG_ADS
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.google.android.gms.ads.AdView
import com.example.ads.utilities.InternetManager
import java.util.concurrent.atomic.AtomicBoolean


class UseCaseBanner(
    private val repositoryBannerImpl: RepositoryBannerImpl,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager,
    private val context: Context
) {

    /**
     * Thread-safe flag to prevent duplicate ad load requests.
     * Using AtomicBoolean instead of @Volatile to ensure atomic operations
     * when checking and setting state from multiple callback threads.
     */
    private val isAdLoading = AtomicBoolean(false)

    private fun checkRemoteConfig(bannerAdKey: BannerAdKey): Boolean {
        return when (bannerAdKey) {
            BannerAdKey.HOME -> sharedPreferenceUtils.rcBannerHome != 0
        }
    }

    private fun getAdId(bannerAdKey: BannerAdKey): String {
        return when (bannerAdKey) {
            BannerAdKey.HOME -> BuildConfig.banner_home
        }
    }

    private fun getFallbackAdId(bannerAdKey: BannerAdKey): String? {
        return when (bannerAdKey) {
            BannerAdKey.HOME -> BuildConfig.banner_home_2f
        }
    }

    private fun getAdType(bannerAdKey: BannerAdKey): BannerAdType {
        return when (bannerAdKey) {
            BannerAdKey.HOME -> BannerAdType.ADAPTIVE
        }
    }

    fun loadBannerAd(adView: AdView, bannerAdKey: BannerAdKey, callback: (ItemBannerAd?) -> Unit) {
        val bannerAdType = getAdType(bannerAdKey)
        validateAndLoadAd(bannerAdKey, callback) { adId ->
            isAdLoading.set(true)
            var primarySucceeded = false
            repositoryBannerImpl.fetchBannerAd(
                adKey = bannerAdKey.value,
                adId = adId,
                bannerAdType = bannerAdType,
                adView = adView
            ) { result ->
                if (result != null) {
                    primarySucceeded = true
                    isAdLoading.set(false)
                    callback.invoke(result)
                } else {
                    if (!primarySucceeded) {
                        val fallbackId = getFallbackAdId(bannerAdKey)
                        if (!fallbackId.isNullOrEmpty()) {
                            Log.d(TAG_ADS, "${bannerAdKey.value} -> loadBanner: primary failed, trying fallback")
                            repositoryBannerImpl.fetchBannerAd(
                                adKey = "${bannerAdKey.value}(fallback)",
                                adId = fallbackId,
                                bannerAdType = bannerAdType,
                                adView = adView
                            ) {
                                isAdLoading.set(false)
                                callback.invoke(it)
                            }
                        } else {
                            isAdLoading.set(false)
                            callback.invoke(null)
                        }
                    }
                }
            }
        }
    }

    private fun validateAndLoadAd(
        bannerAdKey: BannerAdKey,
        callback: (ItemBannerAd?) -> Unit,
        loadAdAction: (adId: String) -> Unit
    ) {
        val isRemoteEnable = checkRemoteConfig(bannerAdKey)
        val adId = getAdId(bannerAdKey)

        when {
            sharedPreferenceUtils.isAppPurchased -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Premium user")
                callback.invoke(null)
            }

            isRemoteEnable.not() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Remote config is off")
                callback.invoke(null)
            }

            internetManager.isInternetConnected.not() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Internet is not connected")
                callback.invoke(null)
            }

            adId.isEmpty() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Ad id is empty")
                callback.invoke(null)
            }

            isAdLoading.get() -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Ad is already loading")
                callback.invoke(null)
            }

            else -> {
                loadAdAction(adId)
            }
        }
    }

    fun destroyBanner(bannerAdKey: BannerAdKey): Boolean {
        val isDestroyed = repositoryBannerImpl.destroyBanner(bannerAdKey.value)
        if (isDestroyed)
            Log.e(TAG_ADS, "${bannerAdKey.value} -> destroyBanner: destroyed")
        return isDestroyed
    }
}