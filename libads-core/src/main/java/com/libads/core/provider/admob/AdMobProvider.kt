package com.libads.core.provider.admob

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import com.libads.core.provider.AdProvider

class AdMobProvider : AdProvider {
    override val name: String = PROVIDER_NAME

    private val interstitialAds = mutableMapOf<String, InterstitialAd>()
    private val rewardedAds = mutableMapOf<String, RewardedAd>()
    private val rewardedInterstitialAds = mutableMapOf<String, RewardedInterstitialAd>()
    private val bannerAds = mutableMapOf<String, AdView>()
    private val appOpenAds = mutableMapOf<String, AppOpenAd>()
    private val appOpenLoadTimes = mutableMapOf<String, Long>()
    private val nativeAds = mutableMapOf<String, NativeAd>()

    override fun initialize(context: Context, onInitialized: (success: Boolean) -> Unit) {
        MobileAds.initialize(context) {
            onInitialized(true)
        }
    }

    override fun load(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        when (adUnit.type) {
            AdType.INTERSTITIAL -> loadInterstitial(context, adUnit, callback)
            AdType.REWARDED -> loadRewarded(context, adUnit, callback)
            AdType.REWARDED_INTERSTITIAL -> loadRewardedInterstitial(context, adUnit, callback)
            AdType.APP_OPEN -> loadAppOpen(context, adUnit, callback)
            else -> callback.onResult(
                AdResult.Failure(adUnit.id, ERROR_UNSUPPORTED, "Unsupported load type: ${adUnit.type}")
            )
        }
    }

    private fun loadInterstitial(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        InterstitialAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAds[adUnit.id] = ad
                    callback.onResult(AdResult.Success(adUnit.id))
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAds.remove(adUnit.id)
                    callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                }
            })
    }

    private fun loadRewarded(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        RewardedAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAds[adUnit.id] = ad
                    callback.onResult(AdResult.Success(adUnit.id))
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAds.remove(adUnit.id)
                    callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                }
            })
    }

    private fun loadRewardedInterstitial(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        RewardedInterstitialAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAds[adUnit.id] = ad
                    callback.onResult(AdResult.Success(adUnit.id))
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAds.remove(adUnit.id)
                    callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                }
            })
    }

    private fun loadAppOpen(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        AppOpenAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAds[adUnit.id] = ad
                    appOpenLoadTimes[adUnit.id] = System.currentTimeMillis()
                    callback.onResult(AdResult.Success(adUnit.id))
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAds.remove(adUnit.id)
                    appOpenLoadTimes.remove(adUnit.id)
                    callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                }
            })
    }

    override fun isReady(adUnit: AdUnit): Boolean {
        return when (adUnit.type) {
            AdType.INTERSTITIAL -> interstitialAds[adUnit.id] != null
            AdType.REWARDED -> rewardedAds[adUnit.id] != null
            AdType.REWARDED_INTERSTITIAL -> rewardedInterstitialAds[adUnit.id] != null
            AdType.BANNER -> bannerAds[adUnit.id] != null
            AdType.APP_OPEN -> isAppOpenReady(adUnit)
            AdType.NATIVE -> nativeAds[adUnit.id] != null
            else -> false
        }
    }

    override fun show(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        when (adUnit.type) {
            AdType.INTERSTITIAL -> showInterstitial(activity, adUnit, callback)
            AdType.REWARDED -> showRewarded(activity, adUnit, callback)
            AdType.REWARDED_INTERSTITIAL -> showRewardedInterstitial(activity, adUnit, callback)
            AdType.APP_OPEN -> showAppOpen(activity, adUnit, callback)
            else -> callback.onAdFailedToShow(ERROR_UNSUPPORTED, "Unsupported show type: ${adUnit.type}")
        }
    }

    private fun showInterstitial(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        val ad = interstitialAds[adUnit.id]
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob interstitial is not ready")
            return
        }
        ad.fullScreenContentCallback = createFullScreenCallback(callback) { interstitialAds.remove(adUnit.id) }
        ad.show(activity)
    }

    private fun showRewarded(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        val ad = rewardedAds[adUnit.id]
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob rewarded is not ready")
            return
        }
        ad.fullScreenContentCallback = createFullScreenCallback(callback) { rewardedAds.remove(adUnit.id) }
        ad.show(activity) { rewardItem ->
            callback.onUserEarnedReward(rewardItem.amount, rewardItem.type)
        }
    }

    private fun showRewardedInterstitial(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        val ad = rewardedInterstitialAds[adUnit.id]
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob rewarded interstitial is not ready")
            return
        }
        ad.fullScreenContentCallback = createFullScreenCallback(callback) { rewardedInterstitialAds.remove(adUnit.id) }
        ad.show(activity) { rewardItem ->
            callback.onUserEarnedReward(rewardItem.amount, rewardItem.type)
        }
    }

    private fun showAppOpen(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        val ad = appOpenAds[adUnit.id]
        if (ad == null || !isAppOpenReady(adUnit)) {
            appOpenAds.remove(adUnit.id)
            appOpenLoadTimes.remove(adUnit.id)
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob app open is not ready")
            return
        }
        ad.fullScreenContentCallback = createFullScreenCallback(callback) {
            appOpenAds.remove(adUnit.id)
            appOpenLoadTimes.remove(adUnit.id)
        }
        ad.show(activity)
    }

    private fun createFullScreenCallback(
        callback: AdShowCallback,
        clear: () -> Unit
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() = callback.onAdShown()
            override fun onAdClicked() = callback.onAdClicked()

            override fun onAdDismissedFullScreenContent() {
                clear()
                callback.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                clear()
                callback.onAdFailedToShow(error.code, error.message)
            }
        }
    }

    override fun renderInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback) {
        when (adUnit.type) {
            AdType.BANNER -> renderBannerInto(container, adUnit, callback)
            AdType.NATIVE -> renderNativeInto(container, adUnit, callback)
            else -> callback.onResult(
                AdResult.Failure(adUnit.id, ERROR_UNSUPPORTED, "Unsupported render type: ${adUnit.type}")
            )
        }
    }

    private fun renderBannerInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback) {
        bannerAds.remove(adUnit.id)?.destroy()
        container.removeAllViews()

        val adView = AdView(container.context)
        adView.adUnitId = adUnit.networkAdUnitId
        adView.setAdSize(getAnchoredAdaptiveBannerSize(container))
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                bannerAds[adUnit.id] = adView
                callback.onResult(AdResult.Success(adUnit.id))
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                bannerAds.remove(adUnit.id)
                callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
            }
        }

        container.addView(adView)
        adView.loadAd(createBannerAdRequest(adUnit))
    }

    private fun renderNativeInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback) {
        val adLoader = AdLoader.Builder(container.context, adUnit.networkAdUnitId)
            .forNativeAd { nativeAd ->
                nativeAds.remove(adUnit.id)?.destroy()
                nativeAds[adUnit.id] = nativeAd

                val nativeAdView = findNativeAdView(container) ?: createNativeAdView(container.context)
                bindNativeAd(nativeAd, nativeAdView)

                if (nativeAdView.parent == null) {
                    container.removeAllViews()
                    container.addView(nativeAdView)
                }
                callback.onResult(AdResult.Success(adUnit.id))
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun findNativeAdView(view: View): NativeAdView? {
        if (view is NativeAdView) return view
        if (view !is ViewGroup) return null

        for (index in 0 until view.childCount) {
            val nativeAdView = findNativeAdView(view.getChildAt(index))
            if (nativeAdView != null) return nativeAdView
        }
        return null
    }

    private fun createNativeAdView(context: Context): NativeAdView {
        val root = NativeAdView(context)
        root.setBackgroundColor(Color.WHITE)
        root.setPadding(dp(context, 12), dp(context, 12), dp(context, 12), dp(context, 12))

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val badge = TextView(context).apply {
            text = "Ad"
            setTextColor(Color.WHITE)
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(context, 6), dp(context, 2), dp(context, 6), dp(context, 2))
            background = GradientDrawable().apply {
                setColor(Color.rgb(23, 115, 234))
                cornerRadius = dp(context, 2).toFloat()
            }
        }
        content.addView(badge, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val mediaView = MediaView(context)
        content.addView(mediaView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 180)).apply {
            topMargin = dp(context, 8)
        })

        val row = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        content.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 8)
        })

        val iconView = ImageView(context)
        row.addView(iconView, LinearLayout.LayoutParams(dp(context, 48), dp(context, 48)))

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        row.addView(textColumn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(context, 8)
        })

        val headlineView = TextView(context).apply {
            setTextColor(Color.BLACK)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        textColumn.addView(headlineView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val bodyView = TextView(context).apply {
            setTextColor(Color.DKGRAY)
            textSize = 12f
            maxLines = 2
        }
        textColumn.addView(bodyView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 4)
        })

        val ctaView = Button(context).apply {
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.rgb(23, 115, 234))
                cornerRadius = dp(context, 6).toFloat()
            }
        }
        content.addView(ctaView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 48)).apply {
            topMargin = dp(context, 8)
        })

        root.mediaView = mediaView
        root.headlineView = headlineView
        root.bodyView = bodyView
        root.iconView = iconView
        root.callToActionView = ctaView
        return root
    }

    private fun bindNativeAd(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        ensureNativeAssetViews(nativeAdView)

        (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

        val bodyView = nativeAdView.bodyView as? TextView
        bodyView?.text = nativeAd.body
        bodyView?.visibility = if (nativeAd.body.isNullOrBlank()) View.GONE else View.VISIBLE

        val iconView = nativeAdView.iconView as? ImageView
        val iconDrawable = nativeAd.icon?.drawable
        iconView?.setImageDrawable(iconDrawable)
        iconView?.visibility = if (iconDrawable == null) View.GONE else View.VISIBLE

        val callToActionView = nativeAdView.callToActionView as? TextView
        callToActionView?.text = nativeAd.callToAction
        callToActionView?.visibility = if (nativeAd.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE

        nativeAdView.setNativeAd(nativeAd)
    }

    private fun ensureNativeAssetViews(nativeAdView: NativeAdView) {
        if (nativeAdView.mediaView == null) {
            nativeAdView.mediaView = nativeAdView.findDescendantByResourceName("mediaView", MediaView::class.java)
        }
        if (nativeAdView.headlineView == null) {
            nativeAdView.headlineView = nativeAdView.findDescendantByResourceName("tvHeadline", TextView::class.java)
        }
        if (nativeAdView.bodyView == null) {
            nativeAdView.bodyView = nativeAdView.findDescendantByResourceName("tvBody", TextView::class.java)
        }
        if (nativeAdView.iconView == null) {
            nativeAdView.iconView = nativeAdView.findDescendantByResourceName("ivIcon", ImageView::class.java)
        }
        if (nativeAdView.callToActionView == null) {
            nativeAdView.callToActionView = nativeAdView.findDescendantByResourceName("btnCallToAction", TextView::class.java)
        }
    }

    private fun <T : View> View.findDescendantByResourceName(name: String, viewClass: Class<T>): T? {
        if (viewClass.isInstance(this) && id != View.NO_ID && resources.getResourceEntryName(id) == name) {
            return viewClass.cast(this)
        }
        if (this !is ViewGroup) return null

        for (index in 0 until childCount) {
            val match = getChildAt(index).findDescendantByResourceName(name, viewClass)
            if (match != null) return match
        }
        return null
    }

    private fun getAnchoredAdaptiveBannerSize(container: ViewGroup): AdSize {
        val displayMetrics = container.resources.displayMetrics
        val widthPixels = if (container.width > 0) container.width else displayMetrics.widthPixels
        val widthDp = (widthPixels / displayMetrics.density).toInt().coerceAtLeast(MIN_BANNER_WIDTH_DP)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(container.context, widthDp)
    }

    private fun createBannerAdRequest(adUnit: AdUnit): AdRequest {
        val builder = AdRequest.Builder()
        val collapsibleValue = when (adUnit.collapsiblePositionType) {
            CollapsiblePositionType.TOP -> "top"
            CollapsiblePositionType.BOTTOM -> "bottom"
            CollapsiblePositionType.NONE -> null
        }

        if (collapsibleValue != null) {
            val extras = Bundle().apply {
                putString("collapsible", collapsibleValue)
            }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        return builder.build()
    }

    override fun destroy(adUnit: AdUnit) {
        interstitialAds.remove(adUnit.id)
        rewardedAds.remove(adUnit.id)
        rewardedInterstitialAds.remove(adUnit.id)
        bannerAds.remove(adUnit.id)?.destroy()
        appOpenAds.remove(adUnit.id)
        appOpenLoadTimes.remove(adUnit.id)
        nativeAds.remove(adUnit.id)?.destroy()
    }

    private fun isAppOpenReady(adUnit: AdUnit): Boolean {
        val loadTime = appOpenLoadTimes[adUnit.id] ?: return false
        val isFresh = System.currentTimeMillis() - loadTime < APP_OPEN_EXPIRATION_MS
        return appOpenAds[adUnit.id] != null && isFresh
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        const val PROVIDER_NAME = "admob"
        private const val MIN_BANNER_WIDTH_DP = 320
        private const val APP_OPEN_EXPIRATION_MS = 4 * 60 * 60 * 1000L
        private const val ERROR_NOT_READY = -10
        private const val ERROR_UNSUPPORTED = -11
    }
}
