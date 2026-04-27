package com.example.ads.mediator

import com.example.ads.appOpen.screen.enums.AppOpenAdKey
import com.example.ads.banner.presentation.enums.BannerAdKey
import com.example.ads.interstitial.enums.InterAdKey
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.ads.rewarded.enums.RewardedAdKey
import com.example.ads.rewarded.enums.RewardedInterAdKey

/**
 * Unified ad key type that wraps all native ad enums.
 * Allows type-safe routing to appropriate config based on ad type.
 */
sealed class AdKey(open val value: String) {
    data class Interstitial(val key: InterAdKey) : AdKey(key.value)
    data class Rewarded(val key: RewardedAdKey) : AdKey(key.value)
    data class RewardedInterstitial(val key: RewardedInterAdKey) : AdKey(key.value)
    data class AppOpen(val key: AppOpenAdKey) : AdKey(key.value)
    data class Banner(val key: BannerAdKey) : AdKey(key.value)
    data class Native(val key: NativeAdKey) : AdKey(key.value)
}
