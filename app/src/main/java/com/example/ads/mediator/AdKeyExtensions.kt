package com.example.ads.mediator

import com.example.ads.appOpen.screen.enums.AppOpenAdKey
import com.example.ads.banner.presentation.enums.BannerAdKey
import com.example.ads.interstitial.enums.InterAdKey
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.ads.rewarded.enums.RewardedAdKey
import com.example.ads.rewarded.enums.RewardedInterAdKey

// ===== Quick Conversions from Native Enums to AdKey =====

fun InterAdKey.toAdKey(): AdKey.Interstitial = AdKey.Interstitial(this)

fun RewardedAdKey.toAdKey(): AdKey.Rewarded = AdKey.Rewarded(this)

fun RewardedInterAdKey.toAdKey(): AdKey.RewardedInterstitial = AdKey.RewardedInterstitial(this)

fun AppOpenAdKey.toAdKey(): AdKey.AppOpen = AdKey.AppOpen(this)

fun BannerAdKey.toAdKey(): AdKey.Banner = AdKey.Banner(this)

fun NativeAdKey.toAdKey(): AdKey.Native = AdKey.Native(this)

// ===== Safe Extraction of Native Enums from AdKey =====

fun AdKey.getInterAdKey(): InterAdKey? = (this as? AdKey.Interstitial)?.key

fun AdKey.getRewardedAdKey(): RewardedAdKey? = (this as? AdKey.Rewarded)?.key

fun AdKey.getRewardedInterAdKey(): RewardedInterAdKey? = (this as? AdKey.RewardedInterstitial)?.key

fun AdKey.getAppOpenAdKey(): AppOpenAdKey? = (this as? AdKey.AppOpen)?.key

fun AdKey.getBannerAdKey(): BannerAdKey? = (this as? AdKey.Banner)?.key

fun AdKey.getNativeAdKey(): NativeAdKey? = (this as? AdKey.Native)?.key
