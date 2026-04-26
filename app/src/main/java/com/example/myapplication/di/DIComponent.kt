package com.example.myapplication.di

import com.example.ads.appOpen.application.AppOpenAdManager
import com.example.ads.appOpen.screen.AppOpenAdsConfig
import com.example.ads.interstitial.InterstitialAdsConfig
import com.example.ads.rewarded.RewardedAdsConfig
import com.example.ads.rewarded.RewardedInterAdsConfig
import com.example.ads.utilities.SharedPreferenceUtils
import com.example.ads.utilities.firebase.RemoteConfiguration
import com.example.ads.utilities.InternetManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject



class DIComponent : KoinComponent {

    // Utils
    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()

    // Managers
    val internetManager by inject<InternetManager>()

    // Remote Configuration
    val remoteConfiguration by inject<RemoteConfiguration>()

    // Admob
    val appOpenAdManager by inject<AppOpenAdManager>()
    val appOpenAdsConfig by inject<AppOpenAdsConfig>()

    val interstitialAdsConfig by inject<InterstitialAdsConfig>()
    val rewardedAdsConfig by inject<RewardedAdsConfig>()
    val rewardedInterAdsConfig by inject<RewardedInterAdsConfig>()
}