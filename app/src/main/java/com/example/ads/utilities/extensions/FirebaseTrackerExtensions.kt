package com.example.ads.utilities.extensions

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.example.ads.utilities.FirebaseTracker

/**
 * Quick extension functions for Firebase event logging.
 */

// ===== Activity Extensions =====

fun Activity.logAnalyticsEvent(
    eventName: String,
    message: String = "",
    params: Map<String, String> = emptyMap()
) {
    FirebaseTracker.logEvent(eventName, message, params)
}

fun Activity.logAnalyticsEventWithUser(
    eventName: String,
    message: String,
    userId: String,
    params: Map<String, String> = emptyMap()
) {
    FirebaseTracker.logEventWithUser(eventName, message, userId, params)
}

// ===== Fragment Extensions =====

fun Fragment.logAnalyticsEvent(
    eventName: String,
    message: String = "",
    params: Map<String, String> = emptyMap()
) {
    FirebaseTracker.logEvent(eventName, message, params)
}

fun Fragment.logAnalyticsEventWithUser(
    eventName: String,
    message: String,
    userId: String,
    params: Map<String, String> = emptyMap()
) {
    FirebaseTracker.logEventWithUser(eventName, message, userId, params)
}

// ===== Context Extensions =====

fun Context.logAnalyticsEvent(
    eventName: String,
    message: String = "",
    params: Map<String, String> = emptyMap()
) {
    FirebaseTracker.logEvent(eventName, message, params)
}

// ===== Quick Event Shortcuts =====

fun Activity.logScreenView(screenName: String) {
    FirebaseTracker.logEvent("screen_view", "User viewed $screenName", mapOf("screen" to screenName))
}

fun Activity.logButtonClick(buttonName: String) {
    FirebaseTracker.logEvent("button_click", "User clicked $buttonName", mapOf("button" to buttonName))
}

fun Activity.logFeatureAccess(featureName: String) {
    FirebaseTracker.logEvent("feature_access", "User accessed $featureName", mapOf("feature" to featureName))
}

fun Activity.logError(errorName: String, errorMessage: String) {
    FirebaseTracker.logEvent("app_error", errorMessage, mapOf("error" to errorName))
}

fun Fragment.logScreenView(screenName: String) {
    FirebaseTracker.logEvent("screen_view", "User viewed $screenName", mapOf("screen" to screenName))
}

fun Fragment.logButtonClick(buttonName: String) {
    FirebaseTracker.logEvent("button_click", "User clicked $buttonName", mapOf("button" to buttonName))
}

fun Fragment.logFeatureAccess(featureName: String) {
    FirebaseTracker.logEvent("feature_access", "User accessed $featureName", mapOf("feature" to featureName))
}

fun Fragment.logError(errorName: String, errorMessage: String) {
    FirebaseTracker.logEvent("app_error", errorMessage, mapOf("error" to errorName))
}
