package com.example.myapplication.ads

import com.example.myapplication.BuildConfig
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.CollapsiblePositionType
import com.libads.core.provider.admob.AdMobProvider

object AdUnits {
    val mainInterstitial = AdUnit(
        id = "main_interstitial",
        type = AdType.INTERSTITIAL,
        networkAdUnitId = BuildConfig.inter_test,
        providerName = AdMobProvider.PROVIDER_NAME
    )

    val mainRewarded = AdUnit(
        id = "main_rewarded",
        type = AdType.REWARDED,
        networkAdUnitId = BuildConfig.reward_test,
        providerName = AdMobProvider.PROVIDER_NAME
    )

    val mainRewardedInterstitial = AdUnit(
        id = "main_rewarded_interstitial",
        type = AdType.REWARDED_INTERSTITIAL,
        networkAdUnitId = BuildConfig.reward_inter_test,
        providerName = AdMobProvider.PROVIDER_NAME
    )

    val mainBanner = AdUnit(
        id = "main_banner",
        type = AdType.BANNER,
        networkAdUnitId = BuildConfig.banner_test,
        providerName = AdMobProvider.PROVIDER_NAME,
        collapsiblePositionType = CollapsiblePositionType.NONE
    )

    val mainNative = AdUnit(
        id = "main_native",
        type = AdType.NATIVE,
        networkAdUnitId = BuildConfig.native_test,
        providerName = AdMobProvider.PROVIDER_NAME
    )

    val appOpenResume = AdUnit(
        id = "app_open_resume",
        type = AdType.APP_OPEN,
        networkAdUnitId = BuildConfig.appopen_resume_test,
        providerName = AdMobProvider.PROVIDER_NAME
    )
}
