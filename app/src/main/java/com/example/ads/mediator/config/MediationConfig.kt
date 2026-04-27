package com.example.ads.mediator.config

/**
 * Global mediation configuration for all ad operations.
 * Configures network priorities, timeouts, loading behavior, and feature flags.
 */
data class MediationConfig(
    // Network priorities (ordered by preference)
    // These are the mediation networks configured in Google Ads
    val networkPriorities: List<String> = listOf(
        "facebook",
        "applovin",
        "vungle",
        "pangle",
        "mintegral",
        "inmobi",
        "ironsource"
    ),

    // Ad type specific load timeouts (in milliseconds)
    val interstitialTimeout: Long = 10000L,
    val rewardedTimeout: Long = 15000L,
    val rewardedInterstitialTimeout: Long = 15000L,
    val appOpenTimeout: Long = 5000L,
    val bannerTimeout: Long = 5000L,
    val nativeTimeout: Long = 10000L,

    // Loading dialog behavior
    val showLoadingDialog: Boolean = true,
    val loadingDialogText: String = "Loading ad...",
    val loadingDialogCancelable: Boolean = false,

    // Retry policy for failed loads
    val maxRetries: Int = 2,
    val retryDelayMs: Long = 1000L,

    // Banner specific settings
    val enableBannerAutoRefresh: Boolean = true,
    val bannerRefreshIntervalMs: Long = 60000L, // 60 seconds

    // Native specific settings
    val enableNativePreloading: Boolean = false,

    // General settings
    val enableLogging: Boolean = true,
    val logTag: String = "AdsMediator"
)
