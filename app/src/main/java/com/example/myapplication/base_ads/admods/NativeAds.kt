package com.example.myapplication.base_ads.admods

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.base_ads.base.BaseAds
import com.example.myapplication.base_ads.interfaces.OnAdmobLoadListener
import com.example.myapplication.base_ads.interfaces.OnAdmobShowListener
import com.example.myapplication.base_ads.utils.AdsEx.logAdRevenue
import com.example.myapplication.base_ads.utils.FirebaseConfigManager
import com.example.myapplication.base_ads.utils.MmpAdEventTracker
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.*
import com.example.myapplication.BuildConfig
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.R

class NativeAds(
    context: Context,
    private val id: String,
    private val adPlacement: String = ""
) : BaseAds(context) {

    companion object {
        private const val TAG = "Tag_nativeAdmob"
        private var indexLoadNative = 0
    }

    private var retryCount = 0
    private val maxRetries = 3
    private val initialRetryDelay = 2000L
    private var indexDebug = 0
    private var canReload = false
    private val handler = Handler(Looper.getMainLooper())

    private val nativeAdLive = MutableLiveData<NativeAd?>()

    fun available(): Boolean = nativeAdLive.value != null

    fun getNativeAdLive(): MutableLiveData<NativeAd?> = nativeAdLive

    private fun setNativeAd(nativeAd: NativeAd) {
        nativeAdLive.value = nativeAd
    }

    fun destroy() {
        Log.i(TAG, "destroy() - $adPlacement")
        handler.removeCallbacksAndMessages(null)
        onAdmobLoadListener = null
        val ad = nativeAdLive.value
        ad?.destroy()
        nativeAdLive.postValue(null)
    }

    // ========================= SHOW AD ==============================
    fun showNative(
        parent: ShimmerFrameLayout,
        nativeAdViewId: Int,
        onNativeShowListener: OnAdmobShowListener?
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            parent.hideShimmer()
            parent.stopShimmer()
            parent.gone()
            onNativeShowListener?.onError("All ads disabled")
            return
        }
//        if (isPro()) {
//            parent.gone()
//            onNativeShowListener?.onShow()
//            return
//        }
        val ad = nativeAdLive.value
        if (ad != null) {
            try {
                enableReload(true)
                val adView = parent.findViewById<NativeAdView>(nativeAdViewId)

                parent.hideShimmer()
                parent.stopShimmer()

                populateNativeAdView(this, ad, adView)
                onNativeShowListener?.onShow()
            } catch (e: Exception) {
                Log.e(TAG, "showNative error: ${e.message}")
                onNativeShowListener?.onError(e.message ?: "error")
                parent.gone()
            }
        } else {
            onNativeShowListener?.onError("")
            parent.hideShimmer()
            parent.stopShimmer()
            parent.gone()
        }
    }

    private fun enableReload(b: Boolean) {
        canReload = b
    }

    /* ========================= LOAD AD ============================== */
    private var timeoutRunnable: Runnable? = null

    fun load(
        listener: OnAdmobLoadListener?,
        timeoutMillis: Long = 10_000L,
        nativeFull: Boolean = false
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) {
            listener?.onError("All ads disabled")
            return
        }
        indexDebug = indexLoadNative++
        Log.i(TAG, "NativeAdmob: $id indexDebug: $indexDebug - $adPlacement")

        enableReload(false)
        onAdmobLoadListener = listener

        // TIMEOUT handling
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        timeoutRunnable = Runnable {
            if (onAdmobLoadListener != null) {
                Log.i(TAG, "load: timeout - $adPlacement")
                val lastListener = onAdmobLoadListener
                onAdmobLoadListener = null
                lastListener?.onError("timeout")
            }
        }
        handler.postDelayed(timeoutRunnable!!, timeoutMillis)

        val builder = AdLoader.Builder(context, id)

        builder.forNativeAd { ad ->
            Log.i(TAG, "onNativeAdLoaded: ${ad.headline ?: ""} - $adPlacement")

            timeoutRunnable?.let { handler.removeCallbacks(it) }
            timeoutRunnable = null

            nativeAdLive.value?.destroy()

            ad.setOnPaidEventListener { value ->
                context.logAdRevenue(
                    adValue = value,
                    adUnitId = adPlacement,
                    responseInfo = ad.responseInfo,
                    adType = "ad_native"
                )
            }

            setNativeAd(ad)

            val lastListener = onAdmobLoadListener
            onAdmobLoadListener = null
            lastListener?.onLoad()
        }

        val videoOptions = if (nativeFull) {
            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
        } else {
            VideoOptions.Builder().setStartMuted(true).build()
        }

        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(MediaAspectRatio.ANY)
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                timeoutRunnable?.let { handler.removeCallbacks(it) }
                timeoutRunnable = null

                val errorDetails =
                    "Code: ${error.code}, Message: ${error.message}, Domain: ${error.domain}"
                Log.e(TAG, "onAdFailedToLoad: $errorDetails - $adPlacement")

                val lastListener = onAdmobLoadListener
                onAdmobLoadListener = null
                lastListener?.onError(errorDetails)
            }

            override fun onAdClicked() {
                MmpAdEventTracker.logClick(
                    context = context,
                    adType = "ad_native",
                    placement = adPlacement,
                    responseInfo = nativeAdLive.value?.responseInfo
                )
            }
        }).build()

        adLoader.loadAd(adRequestBuilder.build())
    }

    fun reLoad() {
        if (!canReload) {
            Log.i(TAG, "not show, disable reload")
            return
        }
        load(null, 30_000, false)
    }

    // ========================= POPULATE VIEW ==============================
    fun populateNativeAdView(
        nativeAdmob: NativeAds,
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        Log.i(TAG, "populateNativeAdView: ")

        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // HEADLINE
        (adView.headlineView as TextView).text =
            if (BuildConfig.DEBUG)
                nativeAd.headline + nativeAdmob.indexDebug
            else nativeAd.headline

        // MEDIA CONTENT
        try {
            adView.mediaView?.mediaContent = nativeAd.mediaContent
        } catch (_: Exception) {
        }

        // BODY
        try {
            if (nativeAd.body == null) {
                adView.bodyView?.visibility = View.INVISIBLE
            } else {
                adView.bodyView?.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }
        } catch (_: Exception) {
        }

        // CTA
        try {
            if (nativeAd.callToAction == null) {
                adView.callToActionView?.visibility = View.INVISIBLE
            } else {
                adView.callToActionView?.visibility = View.VISIBLE
                (adView.callToActionView as TextView).text = nativeAd.callToAction
            }
        } catch (_: Exception) {
        }

        // ICON
        try {
            if (nativeAd.icon == null) {
                adView.iconView?.visibility = View.GONE
            } else {
                (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                adView.iconView?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
        }

        // PRICE
        try {
            if (nativeAd.price == null) adView.priceView?.visibility = View.INVISIBLE
            else {
                adView.priceView?.visibility = View.VISIBLE
                (adView.priceView as TextView).text = nativeAd.price
            }
        } catch (_: Exception) {
        }

        // STORE
        try {
            if (nativeAd.store == null) adView.storeView?.visibility = View.INVISIBLE
            else {
                adView.storeView?.visibility = View.VISIBLE
                (adView.storeView as TextView).text = nativeAd.store
            }
        } catch (_: Exception) {
        }

        // STAR RATING
        try {
            if (nativeAd.starRating == null) adView.starRatingView?.visibility = View.INVISIBLE
            else {
                (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
                adView.starRatingView?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
        }

        // ADVERTISER
        try {
            if (nativeAd.advertiser == null)
                adView.advertiserView?.visibility = View.INVISIBLE
            else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent?.videoController
        vc?.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            override fun onVideoEnd() {
                super.onVideoEnd()
            }
        }

    }

    fun loadWithRetry(
        listener: OnAdmobLoadListener?,
        timeoutMillis: Long = 30000,
        nativeFull: Boolean = false
    ) {
        loadWithRetryInternal(listener, timeoutMillis, nativeFull, retryCount)
    }

    private fun loadWithRetryInternal(
        listener: OnAdmobLoadListener?,
        timeoutMillis: Long,
        nativeFull: Boolean,
        currentRetry: Int
    ) {
        load(object : OnAdmobLoadListener {
            override fun onLoad() {
                retryCount = 0 // Reset retry count on success
                listener?.onLoad()
            }

            override fun onError(e: String) {
                if (currentRetry < maxRetries && shouldRetry(e)) {
                    val delay = initialRetryDelay * (1L shl currentRetry) // Exponential backoff
                    handler.postDelayed({
                        loadWithRetryInternal(listener, timeoutMillis, nativeFull, currentRetry + 1)
                    }, delay)
                    Log.i(
                        TAG,
                        "Retry loading native ad in ${delay}ms (attempt ${currentRetry + 1})"
                    )
                } else {
                    retryCount = 0
                    listener?.onError("Failed after ${currentRetry + 1} attempts: $e")
                }
            }
        }, timeoutMillis, nativeFull)
    }

    private fun shouldRetry(error: String): Boolean {
        return !error.contains("No fill") &&
                !error.contains("Invalid request") &&
                !error.contains("Ad unit ID")
    }
}
