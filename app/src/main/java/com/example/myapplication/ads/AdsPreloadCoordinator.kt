package com.example.myapplication.ads

import android.os.Handler
import android.os.Looper
import com.example.myapplication.utils.firebase.FirebaseConfigManager
import java.util.concurrent.atomic.AtomicBoolean

/** Warms full-screen placements in priority order without flooding mediation at startup. */
object AdsPreloadCoordinator {
    private const val REWARDED_DELAY_MS = 400L
    private const val REWARDED_INTERSTITIAL_DELAY_MS = 800L
    private const val APP_OPEN_DELAY_MS = 1_200L

    private val started = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun start() {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        if (!started.compareAndSet(false, true)) return

        AdInterstitialUtils.preload()
        mainHandler.postDelayed({ AdRewardUtils.preloadRewarded() }, REWARDED_DELAY_MS)
        mainHandler.postDelayed(
            { AdRewardUtils.preloadRewardedInterstitial() },
            REWARDED_INTERSTITIAL_DELAY_MS
        )
        mainHandler.postDelayed(
            { AdOpenResumeUtils.preloadAppOpenResume() },
            APP_OPEN_DELAY_MS
        )
    }
}
