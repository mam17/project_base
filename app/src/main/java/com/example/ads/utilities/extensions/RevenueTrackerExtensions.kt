package com.example.ads.utilities.extensions

import com.example.ads.utilities.RevenueTracker

/**
 * Quick extension functions for revenue tracking.
 */

fun trackAdRevenue(
    revenue: Double,
    source: String = "ads",
    currency: String = "USD"
) {
    RevenueTracker.trackAdImpression(revenue, currency, source)
}

fun trackPurchaseRevenue(
    revenue: Double,
    productId: String,
    quantity: Int = 1,
    currency: String = "USD"
) {
    RevenueTracker.trackPurchase(revenue, currency, productId, quantity)
}

fun trackSubscriptionRevenue(
    revenue: Double,
    subscriptionId: String,
    period: String = "monthly",
    currency: String = "USD"
) {
    RevenueTracker.trackSubscription(revenue, currency, subscriptionId, period)
}

fun trackCustomRevenue(
    eventName: String,
    revenue: Double,
    currency: String = "USD",
    customData: Map<String, Any> = emptyMap()
) {
    RevenueTracker.trackCustomEvent(eventName, revenue, currency, customData)
}
