package com.example.ads.utilities

import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import java.lang.ref.WeakReference
import java.util.UUID

/**
 * Mobile Measurement Partner (MMP) tracker for AppsFlyer.
 * Integrates AppsFlyer for attribution and ROAS tracking.
 * Used for: install tracking, in-app events, revenue tracking, ROAS measurement.
 */
object MMPTracker {

    private const val TAG = "MMPTracker"
    private var contextRef: WeakReference<Context>? = null

    // AppsFlyer Event Names (predefined constants)
    object AppsFlyerEvents {
        const val PURCHASE = "af_purchase"
        const val SUBSCRIBE = "af_subscribe"
        const val AD_IMPRESSION = "af_ad_impression"
        const val LEVEL_ACHIEVED = "af_level_achieved"
        const val TUTORIAL_COMPLETE = "af_tutorial_complete"
        const val CUSTOM_EVENT = "af_custom_event"
    }

    fun initialize(context: Context, appsFlyerId: String) {
        this.contextRef = WeakReference(context)

        try {
            // Initialize AppsFlyer
            initializeAppsFlyer(context, appsFlyerId)
            Log.d(TAG, "AppsFlyer initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AppsFlyer", e)
        }
    }

    // ===== Purchase Events =====

    /**
     * Track in-app purchase for ROAS measurement.
     */
    fun trackPurchase(
        revenue: Double,
        currency: String = "USD",
        productId: String,
        transactionId: String? = null,
        customData: Map<String, String> = emptyMap()
    ) {
        trackToAppsFlyer(
            eventName = AppsFlyerEvents.PURCHASE,
            eventValue = mapOf(
                "af_revenue" to revenue.toString(),
                "af_currency" to currency,
                "af_content_id" to productId,
                "af_transaction_id" to (transactionId ?: UUID.randomUUID().toString())
            ).plus(customData)
        )

        Log.d(TAG, "Purchase tracked: $$revenue $currency")
    }

    /**
     * Track subscription for ROAS measurement.
     */
    fun trackSubscription(
        revenue: Double,
        currency: String = "USD",
        subscriptionId: String,
        period: String = "monthly",
        customData: Map<String, String> = emptyMap()
    ) {
        trackToAppsFlyer(
            eventName = AppsFlyerEvents.SUBSCRIBE,
            eventValue = mapOf(
                "af_revenue" to revenue.toString(),
                "af_currency" to currency,
                "af_subscription_id" to subscriptionId,
                "af_period" to period
            ).plus(customData)
        )

        Log.d(TAG, "Subscription tracked: $$revenue $currency")
    }

    // ===== Ad Revenue Events (for ROAS) =====

    /**
     * Track ad revenue for accurate ROAS calculation.
     */
    fun trackAdRevenue(
        revenue: Double,
        currency: String = "USD",
        source: String = "ads",
        adNetwork: String = "admob",
        customData: Map<String, String> = emptyMap()
    ) {
        trackToAppsFlyer(
            eventName = AppsFlyerEvents.AD_IMPRESSION,
            eventValue = mapOf(
                "af_revenue" to revenue.toString(),
                "af_currency" to currency,
                "af_source" to source,
                "af_ad_network" to adNetwork
            ).plus(customData)
        )

        Log.d(TAG, "Ad revenue tracked: $$revenue from $adNetwork")
    }

    // ===== User Progression Events =====

    /**
     * Track tutorial completion.
     */
    fun trackTutorialComplete(customData: Map<String, String> = emptyMap()) {
        trackToAppsFlyer(
            eventName = AppsFlyerEvents.TUTORIAL_COMPLETE,
            eventValue = customData
        )

        Log.d(TAG, "Tutorial completed")
    }

    /**
     * Track level/achievement reached.
     */
    fun trackLevelAchieved(
        level: Int,
        customData: Map<String, String> = emptyMap()
    ) {
        trackToAppsFlyer(
            eventName = AppsFlyerEvents.LEVEL_ACHIEVED,
            eventValue = mapOf(
                "af_level" to level.toString()
            ).plus(customData)
        )

        Log.d(TAG, "Level achieved: $level")
    }

    // ===== Custom Events =====

    /**
     * Track custom event.
     */
    fun trackCustomEvent(
        eventName: String,
        eventValue: Double? = null,
        currency: String = "USD",
        customData: Map<String, String> = emptyMap()
    ) {
        val afEventValue = mutableMapOf<String, String>()
        afEventValue.putAll(customData)

        if (eventValue != null) {
            afEventValue["af_value"] = eventValue.toString()
            afEventValue["af_currency"] = currency
        }

        trackToAppsFlyer(
            eventName = "${AppsFlyerEvents.CUSTOM_EVENT}_$eventName",
            eventValue = afEventValue
        )

        Log.d(TAG, "Custom event tracked: $eventName")
    }

    // ===== Private Initialization =====

    private fun initializeAppsFlyer(context: Context, appsFlyerId: String) {
        AppsFlyerLib.getInstance().apply {
            setCustomerUserId("")  // Set user ID if available
            start(context, appsFlyerId, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.d(TAG, "AppsFlyer init successful")
                }

                override fun onError(errorCode: Int, errorDesc: String) {
                    Log.e(TAG, "AppsFlyer error: $errorCode - $errorDesc")
                }
            })
        }
    }

    // ===== Private Helpers =====

    private fun trackToAppsFlyer(
        eventName: String,
        eventValue: Map<String, String>
    ) {
        try {
            val ctx = contextRef?.get() ?: return
            AppsFlyerLib.getInstance().logEvent(
                ctx,
                eventName,
                eventValue
            )
            Log.d(TAG, "AppsFlyer event tracked: $eventName")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking to AppsFlyer: $eventName", e)
        }
    }
}
