package com.example.myapplication.base_ads.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.base_ads.admods.BannerAdLoader
import com.example.myapplication.base_ads.admods.BannerAds
import com.example.myapplication.base_ads.helper.CollapsiblePositionType
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.Timer
import kotlin.concurrent.timer

object BannerAdsUntil {
    private const val TAG = "TAG_BannerAdsUntil"

    @SuppressLint("StaticFieldLeak")
    private var bannerAds: BannerAds? = null

    @SuppressLint("StaticFieldLeak")
    private var bannerAdAdaptive: BannerAdLoader? = null
    private val handler = Handler(Looper.getMainLooper())
    private var bannerTimer: Timer? = null

    // ------------------- Banner -------------------
    fun initBannerAdaptive(activity: Activity, adUnit: String, shimmer: ShimmerFrameLayout) {
//        if (SpManager.getInstance(activity).isPro()) {
//            shimmer.visibility = android.view.View.GONE
//            return
//        }
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            shimmer.visibility = android.view.View.GONE
            return
        }
        bannerAdAdaptive = BannerAdLoader(activity, adUnit, shimmer)
        Log.i(TAG, "BannerAd initialized")
    }

    fun loadBanner() {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
//        if (App.instance?.let { SpManager.getInstance(it).isPro() } == true) return
        bannerAdAdaptive?.loadBanner()
    }

    fun destroyBanner() {
        stopBannerAutoReload()
        bannerAdAdaptive?.destroy()
    }

    private fun startBannerAutoReload(interval: Long = 30_000L) {
        stopBannerAutoReload()
        bannerTimer = timer(period = interval) {
            handler.post {
                bannerAdAdaptive?.loadBanner()
            }
        }
    }

    private fun stopBannerAutoReload() {
        bannerTimer?.cancel()
        bannerTimer = null
    }

    fun initBanner(
        activity: Activity,
        primaryAdUnitId: String,
        secondaryAdUnitId: String? = null,
        adPlacement: String,
        shimmer: ShimmerFrameLayout,
        collapsiblePosition: CollapsiblePositionType = CollapsiblePositionType.NONE
    ) {
//        if (SpManager.getInstance(activity).isPro()) {
//            shimmer.visibility = android.view.View.GONE
//            return
//        }
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            shimmer.visibility = android.view.View.GONE
            return
        }
        bannerAds = BannerAds(activity, collapsiblePosition, adPlacement)

        if (!secondaryAdUnitId.isNullOrEmpty()) {
            Log.i(TAG, "Trying to load secondary/floor ad: $secondaryAdUnitId")
            bannerAds?.showBannerWithFallback(
                activity = activity,
                primaryAdUnitId = primaryAdUnitId,
                secondaryAdUnitId = secondaryAdUnitId,
                parent = shimmer
            )
        } else {
            Log.i(TAG, "Loading primary ad only: $primaryAdUnitId")
            bannerAds?.showBanner(activity, primaryAdUnitId, shimmer)
        }
    }


    fun reloadBanner() {
//        if (App.instance?.let { SpManager.getInstance(it).isPro() } == true) return
        bannerAds?.reload()
    }

    fun onResume() {
        bannerAds?.onResume()
    }

    fun onPause() {
        bannerAds?.onPause()
    }

    fun onDestroy() {
        bannerAds?.onDestroy()
        bannerAds = null
    }
}