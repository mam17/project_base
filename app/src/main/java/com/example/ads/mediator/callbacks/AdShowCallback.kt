package com.example.ads.mediator.callbacks

/**
 * Unified callback for showing ads across all ad types.
 * Methods with default implementations are optional for specific ad types.
 */
interface AdShowCallback {
    fun onAdDismissed()
    fun onAdFailedToShow()
    fun onAdShowed() {}
    fun onAdClicked() {}
    fun onRewardEarned() {} // Only used for rewarded/rewarded interstitial ads
}
