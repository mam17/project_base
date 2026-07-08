package com.example.myapplication.base_ads.utils

import android.annotation.SuppressLint
import android.util.Log
import com.example.myapplication.MyApplication
import com.example.myapplication.base_ads.admods.NativeAds
import com.example.myapplication.base_ads.interfaces.OnAdmobLoadListener

object NativeAdsUtil {
    @SuppressLint("StaticFieldLeak")
    var splashNativeFullAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var languageNativeAdmob1: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var languageNativeAdmob2: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var onbNativeAdmob1: NativeAds? = null
    @SuppressLint("StaticFieldLeak")
    var onbNativeAdmob3: NativeAds? = null
    @SuppressLint("StaticFieldLeak")
    var onbNativeFSAdmob1: NativeAds? = null
    @SuppressLint("StaticFieldLeak")
    var onbNativeFSAdmob2: NativeAds? = null


    @SuppressLint("StaticFieldLeak")
    var featureNativeAdmob: NativeAds? = null

    fun loadNativeFullSplash(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            splashNativeFullAdmob?.destroy()
            splashNativeFullAdmob = null
            return
        }
        splashNativeFullAdmob?.destroy()
        val config = FirebaseConfigManager.instance().adConfig

        val isEnabled = if (isFirstOpenApp) {
            config.native_fs_splash_second.enabled
        } else {
            config.native_fs_splash_first.enabled
        }
        if (!isEnabled) return

        val adUnit = if (isFirstOpenApp) config.native_fs_splash_second else config.native_fs_splash_first

        Log.i("NativeAdsUtil", "loadNativeFullSplash: adUnit$adUnit, isFirstOpenApp $isFirstOpenApp")
        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_FS_SPLASH,
            onLoaded = {
                splashNativeFullAdmob = it
                Log.d("NativeAdsUtil", "Splash Native loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d(
                    "NativeAdsUtil",
                    "Splash Native: all IDs failed"
                )
            }
        )
    }

    fun loadNativeLanguage1(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            languageNativeAdmob1?.destroy()
            languageNativeAdmob1 = null
            return
        }
        languageNativeAdmob1?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_language_second_1 else config.native_language_first_1

        if (!adUnit.enabled) return

        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_LANGUAGE_1,
            onLoaded = {
                languageNativeAdmob1 = it
                Log.d("NativeAdsUtil", "loadNativeLanguage1 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeLanguage1: all IDs failed")
            }
        )
    }

    fun loadNativeLanguage2(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            languageNativeAdmob2?.destroy()
            languageNativeAdmob2 = null
            return
        }
        languageNativeAdmob2?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_language_second_1 else config.native_language_first_2

        if (!adUnit.enabled) return

        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_LANGUAGE_2,
            onLoaded = {
                languageNativeAdmob2 = it
                Log.d("NativeAdsUtil", "loadNativeLanguage2 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeLanguage2: all IDs failed")
            }
        )
    }

    fun loadNativeOnb1(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            onbNativeAdmob1?.destroy()
            onbNativeAdmob1 = null
            return
        }
        onbNativeAdmob1?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_ob_second_1 else config.native_ob_first_1
        if (!adUnit.enabled) return

        Log.i(
            "NativeAdsUtil",
            "loadNativeOnb1: idPrimary ${adUnit.id_2f} , idFallback ${adUnit.id}"
        )
        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_ONBOARDING1,
            onLoaded = {
                onbNativeAdmob1 = it
                Log.d("NativeAdsUtil", "loadNativeOnb1 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeOnb1: all IDs failed")
            }
        )
    }

    fun loadNativeFsOnb1(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            onbNativeFSAdmob1?.destroy()
            onbNativeFSAdmob1 = null
            return
        }
        onbNativeFSAdmob1?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_fs_second_1 else config.native_fs_first_1
        if (!adUnit.enabled) return

        Log.i("NativeAdsUtil", "loadNativeFsOnb1: idPrimary ${adUnit.id_2f} , idFallback ${adUnit.id}")
        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_FS_ONBOARDING_1,
            onLoaded = {
                onbNativeFSAdmob1 = it
                Log.d("NativeAdsUtil", "loadNativeFsOnb1 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeFsOnb1: all IDs failed")
            }
        )
    }

    fun loadNativeFsOnb2(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            onbNativeFSAdmob2?.destroy()
            onbNativeFSAdmob2 = null
            return
        }
        onbNativeFSAdmob2?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_fs_second_2 else config.native_fs_first_2
        if (!adUnit.enabled) return

        Log.i(
            "NativeAdsUtil",
            "loadNativeFsOnb2: idPrimary ${adUnit.id_2f} , idFallback ${adUnit.id}"
        )
        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_FS_ONBOARDING_1,
            onLoaded = {
                onbNativeFSAdmob2 = it
                Log.d("NativeAdsUtil", "loadNativeFsOnb2 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeFsOnb2: all IDs failed")
            }
        )
    }

    fun loadNativeOnb3(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            onbNativeAdmob3?.destroy()
            onbNativeAdmob3 = null
            return
        }
        onbNativeAdmob3?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_ob_second_3 else config.native_ob_first_3
        if (!adUnit.enabled) return

        Log.i(
            "NativeAdsUtil",
            "loadNativeOnb3: idPrimary ${adUnit.id_2f} , idFallback ${adUnit.id}"
        )
        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_ONBOARDING3,
            onLoaded = {
                onbNativeAdmob3 = it
                Log.d("NativeAdsUtil", "loadNativeOnb3 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeOnb3: all IDs failed")
            }
        )
    }

    fun loadNativeFeature(
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            featureNativeAdmob?.destroy()
            featureNativeAdmob = null
            return
        }
        featureNativeAdmob?.destroy()
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = config.native_feature_first

        if (!adUnit.enabled) return

        loadWithFallback(
            adUnit = adUnit,
            adPlacement = AdPlacement.NATIVE_PERMISSION,
            onLoaded = {
                featureNativeAdmob = it
                Log.d("NativeAdsUtil", "loadNativeFeature loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("NativeAdsUtil", "loadNativeFeature: all IDs failed")
            }
        )
    }

    fun destroyAllAds() {
        splashNativeFullAdmob?.destroy()
        splashNativeFullAdmob = null

        languageNativeAdmob1?.destroy()
        languageNativeAdmob1 = null

        languageNativeAdmob2?.destroy()
        languageNativeAdmob2 = null

        onbNativeAdmob1?.destroy()
        onbNativeAdmob1 = null

        onbNativeAdmob3?.destroy()
        onbNativeAdmob3 = null

        onbNativeFSAdmob1?.destroy()
        onbNativeFSAdmob1 = null

        onbNativeFSAdmob2?.destroy()
        onbNativeFSAdmob2 = null

        featureNativeAdmob?.destroy()
        featureNativeAdmob = null
    }

    private fun loadWithFallback(
        adUnit: AdUnitConfig,
        adPlacement: String,
        onLoaded: (NativeAds) -> Unit,
        onFailed: (() -> Unit)? = null
    ) {
        val primaryId = adUnit.id_2f.trim().ifBlank { adUnit.id.trim() }
        val fallbackId = adUnit.id.trim().takeIf { it.isNotEmpty() && it != primaryId }

        Log.i("NativeAdsUtils", "loadWithFallback: primaryId $primaryId")
        Log.i("NativeAdsUtils", "loadWithFallback: fallbackId $fallbackId")

        fun loadAd(id: String, next: (() -> Unit)? = null) {
            if (id.isEmpty()) {
                next?.invoke() ?: onFailed?.invoke()
                return
            }

            MyApplication.instance?.applicationContext?.let { context ->
                val finalId = AdsEx.getNativeId(id)
                val nativeAds = NativeAds(context, finalId, adPlacement)
                nativeAds.load(object : OnAdmobLoadListener {
                    override fun onLoad() {
                        onLoaded(nativeAds)
                    }

                    override fun onError(e: String) {
                        next?.invoke() ?: onFailed?.invoke()
                    }
                })
            }
        }

        if (primaryId.isEmpty()) {
            onFailed?.invoke()
            return
        }

        if (fallbackId != null) {
            loadAd(primaryId) {
                loadAd(fallbackId, null)
            }
        } else {
            loadAd(primaryId, null)
        }
    }
}
