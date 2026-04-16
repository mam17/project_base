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
import com.hypersoft.admobads.utilities.manager.InternetManager

/**
 * Created by: Sohaib Ahmed
 * Date: 1/17/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 */

class UseCaseBanner(
    private val repositoryBannerImpl: RepositoryBannerImpl,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager,
    private val context: Context
) {

    @Volatile
    private var isAdLoading = false

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
            isAdLoading = true
            var primarySucceeded = false
            repositoryBannerImpl.fetchBannerAd(
                adKey = bannerAdKey.value,
                adId = adId,
                bannerAdType = bannerAdType,
                adView = adView
            ) { result ->
                if (result != null) {
                    primarySucceeded = true
                    isAdLoading = false
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
                                isAdLoading = false
                                callback.invoke(it)
                            }
                        } else {
                            isAdLoading = false
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

            isAdLoading -> {
                Log.e(TAG_ADS, "${bannerAdKey.value} -> loadBanner: Ad is already loading")
                //callback.invoke(null)
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