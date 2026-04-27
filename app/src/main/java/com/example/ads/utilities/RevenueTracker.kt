package com.example.ads.utilities

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.appevents.AppEventsLogger

/**
 * Utility class to track revenue events for Facebook SDK.
 * Used for app monetization tracking, ad revenue, in-app purchases, etc.
 */
object RevenueTracker {

    private const val TAG = "RevenueTracker"
    private var facebookLogger: AppEventsLogger? = null

    fun initialize(context: Context) {
        try {
            // Initialize Facebook logger
            facebookLogger = AppEventsLogger.newLogger(context)
            Log.d(TAG, "Facebook AppEventsLogger initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Facebook logger", e)
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
    }

    private fun trackToFacebook(
        revenue: Double,
        currency: String,
        eventName: String,
        customData: Map<String, Any>
    ) {
        try {
            val logger = facebookLogger ?: return
            val parameters = Bundle().apply {
                putDouble("_valueToSum", revenue)
                putString("_currency", currency)
                customData.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Double -> putDouble(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }

            val facebookEventName = when (eventName) {
                "Purchase" -> "Purchase"
                "Subscribe" -> "Subscribe"
                "AdImpression" -> "ad_impression"
                else -> eventName
            }

            logger.logEvent(facebookEventName, revenue, parameters)
            Log.d(TAG, "Tracked to Facebook: $facebookEventName - $$revenue $currency")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking to Facebook: $eventName", e)
        }
    }

}
