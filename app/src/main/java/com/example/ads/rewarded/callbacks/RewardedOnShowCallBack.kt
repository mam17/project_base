package com.example.ads.rewarded.callbacks


interface RewardedOnShowCallBack {
    fun onAdDismissedFullScreenContent() {}
    fun onAdFailedToShow()
    fun onAdShowedFullScreenContent() {}
    fun onAdImpression() {}
    fun onUserEarnedReward()
}