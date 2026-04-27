package com.example.ads.utilities

import android.content.Context
import android.util.Log
import com.facebook.appevents.AppEventsLogger
import com.tiktok.business.api.TikTokBusinessSdk
import com.tiktok.business.api.AppEventLogger
import java.math.BigDecimal
import java.util.Currency

/**
 * Utility class to track revenue events for Facebook and TikTok SDKs.
 * Used for app monetization tracking, ad revenue, in-app purchases, etc.
 */
object RevenueTracker {

    private const val TAG = "RevenueTracker"
    private var facebookLogger: AppEventsLogger? = null
    private var tikTokLogger: AppEventLogger? = null

    fun initialize(context: Context) {
        try {
            // Initialize Facebook logger
            facebookLogger = AppEventsLogger.newLogger(context)
            Log.d(TAG, "Facebook AppEventsLogger initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Facebook logger", e)
        }

        try {
            // Initialize TikTok logger
            tikTokLogger = TikTokBusinessSdk.getAppEventLogger()
            Log.d(TAG, "TikTok AppEventLogger initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TikTok logger", e)
        }
    }

    /**
     * Track ad impression revenue (when ads are shown).
     * @param revenue Revenue amount in USD
     * @param currency Currency code (default: USD)
     * @param source Source of revenue (e.g., "interstitial", "rewarded", "banner")
     */
    fun trackAdImpression(
        revenue: Double,
        currency: String = "USD",
        source: String = "ads"
    ) {
        trackRevenue(
            revenue = revenue,
            currency = currency,
            eventName = "AdImpression",
            customData = mapOf(
                "source" to source,
                "type" to "ad_impression"
            )
        )
    }

    /**
     * Track in-app purchase revenue.
     * @param revenue Revenue amount
     * @param currency Currency code
     * @param productId Product ID purchased
     * @param quantity Quantity purchased
     */
    fun trackPurchase(
        revenue: Double,
        currency: String = "USD",
        productId: String,
        quantity: Int = 1
    ) {
        trackRevenue(
            revenue = revenue,
            currency = currency,
            eventName = "Purchase",
            customData = mapOf(
                "product_id" to productId,
                "quantity" to quantity.toString(),
                "type" to "in_app_purchase"
            )
        )
    }

    /**
     * Track subscription revenue.
     * @param revenue Revenue amount
     * @param currency Currency code
     * @param subscriptionId Subscription ID
     * @param period Subscription period (e.g., "monthly", "yearly")
     */
    fun trackSubscription(
        revenue: Double,
        currency: String = "USD",
        subscriptionId: String,
        period: String = "monthly"
    ) {
        trackRevenue(
            revenue = revenue,
            currency = currency,
            eventName = "Subscribe",
            customData = mapOf(
                "subscription_id" to subscriptionId,
                "period" to period,
                "type" to "subscription"
            )
        )
    }

    /**
     * Track custom revenue event.
     * @param eventName Event name to track
     * @param revenue Revenue amount
     * @param currency Currency code
     * @param customData Custom event data
     */
    fun trackCustomEvent(
        eventName: String,
        revenue: Double,
        currency: String = "USD",
        customData: Map<String, Any> = emptyMap()
    ) {
        trackRevenue(
            revenue = revenue,
            currency = currency,
            eventName = eventName,
            customData = customData
        )
    }

    // ===== Private Helpers =====

    private fun trackRevenue(
        revenue: Double,
        currency: String,
        eventName: String,
        customData: Map<String, Any> = emptyMap()
    ) {
        if (revenue <= 0) {
            Log.w(TAG, "Invalid revenue amount: $revenue")
            return
        }

        // Track to Facebook
        trackToFacebook(revenue, currency, eventName, customData)

        // Track to TikTok
        trackToTikTok(revenue, currency, eventName, customData)
    }

    private fun trackToFacebook(
        revenue: Double,
        currency: String,
        eventName: String,
        customData: Map<String, Any>
    ) {
        try {
            val logger = facebookLogger ?: return
            val parameters = customData.toMutableMap().apply {
                put("_valueToSum", revenue)
                put("_currency", currency)
            }

            val facebookEventName = when (eventName) {
                "Purchase" -> AppEventsLogger.EVENT_NAME_PURCHASED
                "Subscribe" -> AppEventsLogger.EVENT_NAME_SUBSCRIBE
                "AdImpression" -> "ad_impression"
                else -> eventName
            }

            logger.logEvent(facebookEventName, BigDecimal(revenue), Currency.getInstance(currency), parameters)
            Log.d(TAG, "Tracked to Facebook: $facebookEventName - $$revenue $currency")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking to Facebook: $eventName", e)
        }
    }

    private fun trackToTikTok(
        revenue: Double,
        currency: String,
        eventName: String,
        customData: Map<String, Any>
    ) {
        try {
            val logger = tikTokLogger ?: return

            val tikTokEventName = when (eventName) {
                "Purchase" -> "Purchase"
                "Subscribe" -> "Subscribe"
                "AdImpression" -> "AdImpression"
                else -> eventName
            }

            val properties = customData.toMutableMap().apply {
                put("value", revenue.toString())
                put("currency", currency)
                put("timestamp", System.currentTimeMillis())
            }

            logger.trackEvent(tikTokEventName, properties)
            Log.d(TAG, "Tracked to TikTok: $tikTokEventName - $$revenue $currency")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking to TikTok: $eventName", e)
        }
    }
}
