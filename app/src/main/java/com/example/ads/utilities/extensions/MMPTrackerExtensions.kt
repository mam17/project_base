package com.example.ads.utilities.extensions

import com.example.ads.utilities.MMPTracker

/**
 * Quick extension functions for MMP tracking (Adjust & AppsFlyer).
 */

// ===== Purchase & Revenue =====

fun trackMmpPurchase(
    revenue: Double,
    productId: String,
    currency: String = "USD",
    transactionId: String? = null,
    customData: Map<String, String> = emptyMap()
) {
    MMPTracker.trackPurchase(revenue, currency, productId, transactionId, customData)
}

fun trackMmpSubscription(
    revenue: Double,
    subscriptionId: String,
    period: String = "monthly",
    currency: String = "USD",
    customData: Map<String, String> = emptyMap()
) {
    MMPTracker.trackSubscription(revenue, currency, subscriptionId, period, customData)
}

fun trackMmpAdRevenue(
    revenue: Double,
    source: String = "ads",
    adNetwork: String = "admob",
    currency: String = "USD",
    customData: Map<String, String> = emptyMap()
) {
    MMPTracker.trackAdRevenue(revenue, currency, source, adNetwork, customData)
}

// ===== User Events =====

fun trackMmpTutorialComplete(customData: Map<String, String> = emptyMap()) {
    MMPTracker.trackTutorialComplete(customData)
}

fun trackMmpLevelAchieved(
    level: Int,
    customData: Map<String, String> = emptyMap()
) {
    MMPTracker.trackLevelAchieved(level, customData)
}

fun trackMmpCustomEvent(
    eventName: String,
    eventValue: Double? = null,
    currency: String = "USD",
    customData: Map<String, String> = emptyMap()
) {
    MMPTracker.trackCustomEvent(eventName, eventValue, currency, customData)
}
