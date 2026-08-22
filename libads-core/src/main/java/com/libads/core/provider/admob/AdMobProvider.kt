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
import com.google.android.gms.ads.OnPaidEventListener
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
import com.libads.core.callback.AdMediationInfo
import com.libads.core.callback.AdRevenue
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import com.libads.core.provider.AdProvider
import com.libads.core.util.AdEventType
import com.libads.core.util.AdLogger

class AdMobProvider : AdProvider {
    override val name: String = PROVIDER_NAME

    private enum class InitializationState {
        NOT_STARTED,
        INITIALIZING,
        READY,
        FAILED
    }

    private data class PendingInitializationAction(
        val action: () -> Unit,
        val onFailure: () -> Unit
    )

    private data class CachedAd<T>(
        val networkAdUnitId: String,
        val value: T,
        val loadedAtMillis: Long = System.currentTimeMillis()
    )

    private val cacheLock = Any()
    private val initializationLock = Any()
    private var initializationState = InitializationState.NOT_STARTED
    private val pendingInitializationActions = mutableListOf<PendingInitializationAction>()
    private val loadGenerations = mutableMapOf<String, Long>()
    private val interstitialAds = mutableMapOf<String, CachedAd<InterstitialAd>>()
    private val rewardedAds = mutableMapOf<String, CachedAd<RewardedAd>>()
    private val rewardedInterstitialAds = mutableMapOf<String, CachedAd<RewardedInterstitialAd>>()
    private val bannerAds = mutableMapOf<String, CachedAd<AdView>>()
    private val appOpenAds = mutableMapOf<String, CachedAd<AppOpenAd>>()
    private val nativeAds = mutableMapOf<String, CachedAd<NativeAd>>()

    override fun initialize(context: Context, onInitialized: (success: Boolean) -> Unit) {
        runWhenInitialized(
            context = context.applicationContext,
            action = { onInitialized(true) },
            onFailure = { onInitialized(false) }
        )
    }

    override fun load(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        runWhenInitialized(
            context = context.applicationContext,
            action = {
                when (adUnit.type) {
                    AdType.INTERSTITIAL -> loadInterstitial(context, adUnit, callback)
                    AdType.REWARDED -> loadRewarded(context, adUnit, callback)
                    AdType.REWARDED_INTERSTITIAL -> loadRewardedInterstitial(context, adUnit, callback)
                    AdType.APP_OPEN -> loadAppOpen(context, adUnit, callback)
                    AdType.NATIVE -> loadNative(context, adUnit, callback)
                    else -> callback.onResult(
                        AdResult.Failure(adUnit.id, ERROR_UNSUPPORTED, "Unsupported load type: ${adUnit.type}")
                    )
                }
            },
            onFailure = {
                callback.onResult(
                    AdResult.Failure(adUnit.id, ERROR_INITIALIZATION, "AdMob initialization failed")
                )
            }
        )
    }

    private fun loadInterstitial(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        val generation = beginLoad(adUnit)
        InterstitialAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    if (storeIfCurrent(adUnit, generation, interstitialAds, ad)) {
                        callback.onResult(AdResult.Success(adUnit.id))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (removeIfCurrent(adUnit, generation, interstitialAds)) {
                        callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }
            })
    }

    private fun loadRewarded(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        val generation = beginLoad(adUnit)
        RewardedAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    if (storeIfCurrent(adUnit, generation, rewardedAds, ad)) {
                        callback.onResult(AdResult.Success(adUnit.id))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (removeIfCurrent(adUnit, generation, rewardedAds)) {
                        callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }
            })
    }

    private fun loadRewardedInterstitial(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        val generation = beginLoad(adUnit)
        RewardedInterstitialAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    if (storeIfCurrent(adUnit, generation, rewardedInterstitialAds, ad)) {
                        callback.onResult(AdResult.Success(adUnit.id))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (removeIfCurrent(adUnit, generation, rewardedInterstitialAds)) {
                        callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }
            })
    }

    private fun loadAppOpen(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        val generation = beginLoad(adUnit)
        AppOpenAd.load(context, adUnit.networkAdUnitId, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    if (storeIfCurrent(adUnit, generation, appOpenAds, ad)) {
                        callback.onResult(AdResult.Success(adUnit.id))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (removeIfCurrent(adUnit, generation, appOpenAds)) {
                        callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }
            })
    }

    private fun loadNative(context: Context, adUnit: AdUnit, callback: AdLoadCallback) {
        val generation = beginLoad(adUnit)
        val adLoader = AdLoader.Builder(context, adUnit.networkAdUnitId)
            .forNativeAd { nativeAd ->
                if (!isCurrent(adUnit, generation)) {
                    nativeAd.destroy()
                    callback.onResult(supersededResult(adUnit))
                    return@forNativeAd
                }
                synchronized(cacheLock) { nativeAds.remove(adUnit.id) }?.value?.destroy()
                if (storeIfCurrent(adUnit, generation, nativeAds, nativeAd)) {
                    callback.onResult(AdResult.Success(adUnit.id))
                } else {
                    nativeAd.destroy()
                    callback.onResult(supersededResult(adUnit))
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (removeIfCurrent(adUnit, generation, nativeAds)) {
                        callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun isReady(adUnit: AdUnit): Boolean {
        return when (adUnit.type) {
            AdType.INTERSTITIAL -> isCachedFor(interstitialAds, adUnit)
            AdType.REWARDED -> isCachedFor(rewardedAds, adUnit)
            AdType.REWARDED_INTERSTITIAL -> isCachedFor(rewardedInterstitialAds, adUnit)
            AdType.BANNER -> isCachedFor(bannerAds, adUnit)
            AdType.APP_OPEN -> isAppOpenReady(adUnit)
            AdType.NATIVE -> isCachedFor(nativeAds, adUnit)
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
        val ad = takeCached(interstitialAds, adUnit)
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob interstitial is not ready")
            return
        }
        val mediationInfo = ad.responseInfo.toAdMediationInfo()
        ad.fullScreenContentCallback = createFullScreenCallback(callback, mediationInfo)
        ad.onPaidEventListener = createPaidEventListener(callback, mediationInfo)
        ad.show(activity)
    }

    private fun showRewarded(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        val ad = takeCached(rewardedAds, adUnit)
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob rewarded is not ready")
            return
        }
        val mediationInfo = ad.responseInfo.toAdMediationInfo()
        ad.fullScreenContentCallback = createFullScreenCallback(callback, mediationInfo)
        ad.onPaidEventListener = createPaidEventListener(callback, mediationInfo)
        ad.show(activity) { rewardItem ->
            callback.onUserEarnedReward(rewardItem.amount, rewardItem.type)
        }
    }

    private fun showRewardedInterstitial(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        val ad = takeCached(rewardedInterstitialAds, adUnit)
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob rewarded interstitial is not ready")
            return
        }
        val mediationInfo = ad.responseInfo.toAdMediationInfo()
        ad.fullScreenContentCallback = createFullScreenCallback(callback, mediationInfo)
        ad.onPaidEventListener = createPaidEventListener(callback, mediationInfo)
        ad.show(activity) { rewardItem ->
            callback.onUserEarnedReward(rewardItem.amount, rewardItem.type)
        }
    }

    private fun showAppOpen(activity: Activity, adUnit: AdUnit, callback: AdShowCallback) {
        if (!isAppOpenReady(adUnit)) {
            synchronized(cacheLock) { appOpenAds.remove(adUnit.id) }
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob app open is not ready")
            return
        }
        val ad = takeCached(appOpenAds, adUnit)
        if (ad == null) {
            callback.onAdFailedToShow(ERROR_NOT_READY, "AdMob app open is not ready")
            return
        }
        val mediationInfo = ad.responseInfo.toAdMediationInfo()
        ad.fullScreenContentCallback = createFullScreenCallback(callback, mediationInfo)
        ad.onPaidEventListener = createPaidEventListener(callback, mediationInfo)
        ad.show(activity)
    }

    private fun createFullScreenCallback(
        callback: AdShowCallback,
        mediationInfo: AdMediationInfo?
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() = callback.onAdShown()
            override fun onAdImpression() = callback.onAdImpression(mediationInfo)
            override fun onAdClicked() = callback.onAdClicked(mediationInfo)

            override fun onAdDismissedFullScreenContent() {
                callback.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                callback.onAdFailedToShow(error.code, error.message)
            }
        }
    }

    override fun renderInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback) {
        runWhenInitialized(
            context = container.context.applicationContext,
            action = {
                when (adUnit.type) {
                    AdType.BANNER -> renderBannerInto(container, adUnit, callback)
                    AdType.NATIVE -> renderNativeInto(container, adUnit, callback)
                    else -> callback.onResult(
                        AdResult.Failure(adUnit.id, ERROR_UNSUPPORTED, "Unsupported render type: ${adUnit.type}")
                    )
                }
            },
            onFailure = {
                callback.onResult(
                    AdResult.Failure(adUnit.id, ERROR_INITIALIZATION, "AdMob initialization failed")
                )
            }
        )
    }

    private fun renderBannerInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback) {
        val generation = beginLoad(adUnit)
        synchronized(cacheLock) { bannerAds.remove(adUnit.id) }?.value?.destroy()
        container.removeAllViews()

        val adView = AdView(container.context)
        adView.adUnitId = adUnit.networkAdUnitId
        adView.setAdSize(getAnchoredAdaptiveBannerSize(container))
        adView.onPaidEventListener = OnPaidEventListener { adValue ->
            AdLogger.event(
                adUnit,
                AdEventType.PAID,
                revenue = adValue.toAdRevenue(),
                mediationInfo = adView.responseInfo.toAdMediationInfo()
            )
        }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (storeIfCurrent(adUnit, generation, bannerAds, adView)) {
                    callback.onResult(AdResult.Success(adUnit.id))
                } else {
                    adView.destroy()
                    callback.onResult(supersededResult(adUnit))
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                if (removeIfCurrent(adUnit, generation, bannerAds)) {
                    callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                } else {
                    callback.onResult(supersededResult(adUnit))
                }
            }

            override fun onAdImpression() {
                AdLogger.event(
                    adUnit,
                    AdEventType.IMPRESSION,
                    mediationInfo = adView.responseInfo.toAdMediationInfo()
                )
            }

            override fun onAdClicked() {
                AdLogger.event(
                    adUnit,
                    AdEventType.CLICKED,
                    mediationInfo = adView.responseInfo.toAdMediationInfo()
                )
            }
        }

        container.addView(adView)
        adView.loadAd(createBannerAdRequest(adUnit))
    }

    private fun renderNativeInto(container: ViewGroup, adUnit: AdUnit, callback: AdLoadCallback) {
        val cachedNative = takeCached(nativeAds, adUnit)
        if (cachedNative != null) {
            cachedNative.setOnPaidEventListener { adValue ->
                AdLogger.event(
                    adUnit,
                    AdEventType.PAID,
                    revenue = adValue.toAdRevenue(),
                    mediationInfo = cachedNative.responseInfo.toAdMediationInfo()
                )
            }
            val nativeAdView = findNativeAdView(container) ?: createNativeAdView(container.context)
            bindNativeAd(cachedNative, nativeAdView)
            if (nativeAdView.parent == null) {
                container.removeAllViews()
                container.addView(nativeAdView)
            }
            AdLogger.event(
                adUnit,
                AdEventType.IMPRESSION,
                mediationInfo = cachedNative.responseInfo.toAdMediationInfo()
            )
            callback.onResult(AdResult.Success(adUnit.id))
            return
        }

        val generation = beginLoad(adUnit)
        var loadedNativeAd: NativeAd? = null
        val adLoader = AdLoader.Builder(container.context, adUnit.networkAdUnitId)
            .forNativeAd { nativeAd ->
                if (!isCurrent(adUnit, generation)) {
                    nativeAd.destroy()
                    callback.onResult(supersededResult(adUnit))
                    return@forNativeAd
                }
                synchronized(cacheLock) { nativeAds.remove(adUnit.id) }?.value?.destroy()
                if (!storeIfCurrent(adUnit, generation, nativeAds, nativeAd)) {
                    nativeAd.destroy()
                    callback.onResult(supersededResult(adUnit))
                    return@forNativeAd
                }
                loadedNativeAd = nativeAd

                nativeAd.setOnPaidEventListener { adValue ->
                    AdLogger.event(
                        adUnit,
                        AdEventType.PAID,
                        revenue = adValue.toAdRevenue(),
                        mediationInfo = nativeAd.responseInfo.toAdMediationInfo()
                    )
                }

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
                    if (removeIfCurrent(adUnit, generation, nativeAds)) {
                        callback.onResult(AdResult.Failure(adUnit.id, error.code, error.message))
                    } else {
                        callback.onResult(supersededResult(adUnit))
                    }
                }

                override fun onAdImpression() {
                    AdLogger.event(
                        adUnit,
                        AdEventType.IMPRESSION,
                        mediationInfo = loadedNativeAd?.responseInfo.toAdMediationInfo()
                    )
                }

                override fun onAdClicked() {
                    AdLogger.event(
                        adUnit,
                        AdEventType.CLICKED,
                        mediationInfo = loadedNativeAd?.responseInfo.toAdMediationInfo()
                    )
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
        return AdSize.getLargeAnchoredAdaptiveBannerAdSize(container.context, widthDp)
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
        invalidateLoad(adUnit)
        when (adUnit.type) {
            AdType.INTERSTITIAL -> synchronized(cacheLock) { interstitialAds.remove(adUnit.id) }
            AdType.REWARDED -> synchronized(cacheLock) { rewardedAds.remove(adUnit.id) }
            AdType.REWARDED_INTERSTITIAL -> synchronized(cacheLock) {
                rewardedInterstitialAds.remove(adUnit.id)
            }
            AdType.BANNER -> synchronized(cacheLock) { bannerAds.remove(adUnit.id) }?.value?.destroy()
            AdType.APP_OPEN -> synchronized(cacheLock) { appOpenAds.remove(adUnit.id) }
            AdType.NATIVE -> synchronized(cacheLock) { nativeAds.remove(adUnit.id) }?.value?.destroy()
        }
    }

    override fun destroyAll() {
        val banners: List<AdView>
        val natives: List<NativeAd>
        synchronized(cacheLock) {
            banners = bannerAds.values.map { it.value }
            natives = nativeAds.values.map { it.value }
            interstitialAds.clear()
            rewardedAds.clear()
            rewardedInterstitialAds.clear()
            bannerAds.clear()
            appOpenAds.clear()
            nativeAds.clear()
            loadGenerations.replaceAll { _, generation -> generation + 1L }
        }
        banners.forEach(AdView::destroy)
        natives.forEach(NativeAd::destroy)
    }

    private fun isAppOpenReady(adUnit: AdUnit): Boolean {
        val cached = synchronized(cacheLock) { appOpenAds[adUnit.id] } ?: return false
        if (cached.networkAdUnitId != adUnit.networkAdUnitId) {
            synchronized(cacheLock) { appOpenAds.remove(adUnit.id) }
            return false
        }
        return System.currentTimeMillis() - cached.loadedAtMillis < APP_OPEN_EXPIRATION_MS
    }

    private fun runWhenInitialized(
        context: Context,
        action: () -> Unit,
        onFailure: () -> Unit
    ) {
        var startInitialization = false
        var runNow = false
        var failNow = false
        synchronized(initializationLock) {
            when (initializationState) {
                InitializationState.READY -> runNow = true
                InitializationState.FAILED -> failNow = true
                InitializationState.INITIALIZING -> {
                    pendingInitializationActions += PendingInitializationAction(action, onFailure)
                }
                InitializationState.NOT_STARTED -> {
                    initializationState = InitializationState.INITIALIZING
                    pendingInitializationActions += PendingInitializationAction(action, onFailure)
                    startInitialization = true
                }
            }
        }

        when {
            runNow -> action()
            failNow -> onFailure()
            startInitialization -> startMobileAdsInitialization(context)
        }
    }

    private fun startMobileAdsInitialization(context: Context) {
        synchronized(initializationLock) {
            initializationState = InitializationState.READY
        }
        try {
            MobileAds.initialize(context) {
                // Background mediation adapters completed
            }
        } catch (throwable: Throwable) {
            AdLogger.e("MobileAds.initialize failed", throwable)
        }
        val actions = synchronized(initializationLock) {
            pendingInitializationActions.toList().also { pendingInitializationActions.clear() }
        }
        actions.forEach { it.action() }
    }

    private fun beginLoad(adUnit: AdUnit): Long = synchronized(cacheLock) {
        val key = requestKey(adUnit)
        val generation = (loadGenerations[key] ?: 0L) + 1L
        loadGenerations[key] = generation
        generation
    }

    private fun invalidateLoad(adUnit: AdUnit) {
        synchronized(cacheLock) {
            val key = requestKey(adUnit)
            loadGenerations[key] = (loadGenerations[key] ?: 0L) + 1L
        }
    }

    private fun isCurrent(adUnit: AdUnit, generation: Long): Boolean = synchronized(cacheLock) {
        loadGenerations[requestKey(adUnit)] == generation
    }

    private fun <T> storeIfCurrent(
        adUnit: AdUnit,
        generation: Long,
        target: MutableMap<String, CachedAd<T>>,
        value: T
    ): Boolean = synchronized(cacheLock) {
        if (loadGenerations[requestKey(adUnit)] != generation) return@synchronized false
        target[adUnit.id] = CachedAd(adUnit.networkAdUnitId, value)
        true
    }

    private fun <T> removeIfCurrent(
        adUnit: AdUnit,
        generation: Long,
        target: MutableMap<String, CachedAd<T>>
    ): Boolean = synchronized(cacheLock) {
        if (loadGenerations[requestKey(adUnit)] != generation) return@synchronized false
        target.remove(adUnit.id)
        true
    }

    private fun <T> isCachedFor(
        target: MutableMap<String, CachedAd<T>>,
        adUnit: AdUnit
    ): Boolean = synchronized(cacheLock) {
        val cached = target[adUnit.id] ?: return@synchronized false
        if (cached.networkAdUnitId != adUnit.networkAdUnitId) {
            target.remove(adUnit.id)
            return@synchronized false
        }
        true
    }

    private fun <T> takeCached(
        target: MutableMap<String, CachedAd<T>>,
        adUnit: AdUnit
    ): T? = synchronized(cacheLock) {
        val cached = target[adUnit.id] ?: return@synchronized null
        if (cached.networkAdUnitId != adUnit.networkAdUnitId) {
            target.remove(adUnit.id)
            return@synchronized null
        }
        target.remove(adUnit.id)?.value
    }

    private fun requestKey(adUnit: AdUnit): String = "${adUnit.type}|${adUnit.id}"

    private fun createPaidEventListener(
        callback: AdShowCallback,
        mediationInfo: AdMediationInfo?
    ): OnPaidEventListener {
        return OnPaidEventListener { adValue ->
            callback.onPaidEvent(adValue.toAdRevenue(), mediationInfo)
        }
    }

    private fun com.google.android.gms.ads.AdValue.toAdRevenue() = AdRevenue(
        valueMicros = valueMicros,
        currencyCode = currencyCode,
        precisionType = precisionType
    )

    private fun supersededResult(adUnit: AdUnit): AdResult.Failure = AdResult.Failure(
        adUnitId = adUnit.id,
        errorCode = ERROR_REQUEST_SUPERSEDED,
        message = "Ad request was superseded by a newer request"
    )

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        const val PROVIDER_NAME = "admob"
        private const val MIN_BANNER_WIDTH_DP = 320
        private const val APP_OPEN_EXPIRATION_MS = 4 * 60 * 60 * 1000L
        private const val ERROR_NOT_READY = -10
        private const val ERROR_UNSUPPORTED = -11
        private const val ERROR_REQUEST_SUPERSEDED = -12
        private const val ERROR_INITIALIZATION = -13
    }
}
