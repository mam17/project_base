package com.example.myapplication.ads

import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

object AdsPreloadCoordinator {
    private const val REWARDED_DELAY_MS = 400L
    private const val REWARDED_INTERSTITIAL_DELAY_MS = 800L
    private const val APP_OPEN_DELAY_MS = 1_200L

    private val started = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun start() {
        if (!RemoteAdConfig.isEnableAllAds) return
        if (!started.compareAndSet(false, true)) return

        Ads.preload(AdPlacement.INTER_FEATURE)
        Ads.preload(AdPlacement.NATIVE_FEATURE)
        mainHandler.postDelayed({
            Ads.preload(AdPlacement.REWARD)
        }, REWARDED_DELAY_MS)
        mainHandler.postDelayed({
            Ads.preload(AdPlacement.REWARD_INTER)
        }, REWARDED_INTERSTITIAL_DELAY_MS)
        mainHandler.postDelayed({
            AdAppOpenManager.preload()
        }, APP_OPEN_DELAY_MS)
    }
}
