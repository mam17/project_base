package com.example.ads.natives.domain.useCases

import android.content.Context
import android.util.Log
import java.util.Collections
import com.example.ads.natives.data.entities.ItemNativeAd
import com.example.ads.natives.data.repositories.RepositoryNativeImpl
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.ads.utilities.Constants.TAG_ADS
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.myapplication.BuildConfig
import com.example.ads.utilities.InternetManager



class UseCaseNative(
    private val repositoryNativeImpl: RepositoryNativeImpl,
    private val sharedPreferenceUtils: SharedPreferenceUtils,
    private val internetManager: InternetManager,
    private val context: Context
) {

    private val loadingKeys = Collections.synchronizedSet(mutableSetOf<String>())

    private fun checkRemoteConfig(nativeAdKey: NativeAdKey): Boolean {
        return when (nativeAdKey) {
            NativeAdKey.LANGUAGE_1, NativeAdKey.LANGUAGE_2 -> sharedPreferenceUtils.rcNativeLanguage != 0
            NativeAdKey.ON_BOARDING_1, NativeAdKey.ON_BOARDING_2, NativeAdKey.ON_BOARDING_3, NativeAdKey.ON_BOARDING_FS_1, NativeAdKey.ON_BOARDING_FS_2 -> sharedPreferenceUtils.rcNativeOnBoarding != 0
            NativeAdKey.HOME -> sharedPreferenceUtils.rcNativeHome != 0
            NativeAdKey.FEATURE -> sharedPreferenceUtils.rcNativeFeature != 0
        }
    }

    private fun getAdId(nativeAdKey: NativeAdKey): String {
        return when (nativeAdKey) {
            NativeAdKey.LANGUAGE_1 -> BuildConfig.native_language_1_1
            NativeAdKey.LANGUAGE_2 -> BuildConfig.native_language_2_1
            NativeAdKey.ON_BOARDING_1 -> BuildConfig.native_ob_1
            NativeAdKey.ON_BOARDING_2 -> BuildConfig.native_ob_2
            NativeAdKey.ON_BOARDING_3 -> BuildConfig.native_ob_3
            NativeAdKey.ON_BOARDING_FS_1 -> BuildConfig.native_fs_1
            NativeAdKey.ON_BOARDING_FS_2 -> BuildConfig.native_fs_2
            NativeAdKey.HOME -> BuildConfig.native_home
            NativeAdKey.FEATURE -> BuildConfig.native_feature
        }
    }

    private fun getFallbackAdId(nativeAdKey: NativeAdKey): String? {
        return when (nativeAdKey) {
            NativeAdKey.LANGUAGE_1 -> BuildConfig.native_language_1_2
            NativeAdKey.LANGUAGE_2 -> BuildConfig.native_language_2_2
            NativeAdKey.ON_BOARDING_FS_1 -> BuildConfig.native_fs_1_2f
            NativeAdKey.ON_BOARDING_FS_2 -> BuildConfig.native_fs_2_2f
            NativeAdKey.HOME -> BuildConfig.native_home_2f
            else -> null
        }
    }

    fun loadNativeAd(nativeAdKey: NativeAdKey, callback: (ItemNativeAd?) -> Unit) {
        validateAndLoadAd(nativeAdKey, callback) { adId ->
            loadingKeys.add(nativeAdKey.value)
            var primarySucceeded = false
            repositoryNativeImpl.fetchNativeAd(adKey = nativeAdKey.value, adId = adId) { result ->
                if (result != null) {
                    primarySucceeded = true
                    loadingKeys.remove(nativeAdKey.value)
                    callback.invoke(result)
                } else {
                    if (!primarySucceeded) {
                        val fallbackId = getFallbackAdId(nativeAdKey)
                        if (!fallbackId.isNullOrEmpty()) {
                            Log.d(TAG_ADS, "${nativeAdKey.value} -> loadNative: primary failed, trying fallback")
                            repositoryNativeImpl.fetchNativeAd(
                                adKey = "${nativeAdKey.value}(fallback)",
                                adId = fallbackId
                            ) {
                                loadingKeys.remove(nativeAdKey.value)
                                callback.invoke(it)
                            }
                        } else {
                            loadingKeys.remove(nativeAdKey.value)
                            callback.invoke(null)
                        }
                    }
                }
            }
        }
    }

    private fun validateAndLoadAd(
        nativeAdKey: NativeAdKey,
        callback: (ItemNativeAd?) -> Unit,
        loadAdAction: (adId: String) -> Unit
    ) {
        val isRemoteEnable = checkRemoteConfig(nativeAdKey)
        val adId = getAdId(nativeAdKey)

        when {
            sharedPreferenceUtils.isAppPurchased -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Premium user")
                callback.invoke(null)
            }

            isRemoteEnable.not() -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Remote config is off")
                callback.invoke(null)
            }

            internetManager.isInternetConnected.not() -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Internet is not connected")
                callback.invoke(null)
            }

            adId.isEmpty() -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Ad id is empty")
                callback.invoke(null)
            }

            loadingKeys.contains(nativeAdKey.value) -> {
                Log.e(TAG_ADS, "${nativeAdKey.value} -> loadNative: Ad is already loading")
            }

            else -> {
                loadAdAction(adId)
            }
        }
    }

    fun destroyNative(nativeAdKey: NativeAdKey): Boolean {
        val isDestroyed = repositoryNativeImpl.destroyNative(nativeAdKey.value)
        if (isDestroyed)
            Log.e(TAG_ADS, "${nativeAdKey.value} -> destroyNative: destroyed")
        return isDestroyed
    }
}