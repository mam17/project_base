package com.example.ads.utilities

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Unified Firebase Analytics tracker for event logging.
 * Tracks custom events and user actions for analytics and debugging.
 */
object FirebaseTracker {

    private const val TAG = "FirebaseTracker"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            Log.d(TAG, "Firebase Analytics initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Analytics", e)
        }
    }

    /**
     * Log custom event to Firebase Analytics.
     * @param eventName Event identifier (max 40 chars, alphanumeric & underscore only)
     * @param message Event message/description for debugging
     * @param params Optional custom parameters (key max 24 chars, value max 100 chars)
     */
    fun logEvent(
        eventName: String,
        message: String,
        params: Map<String, String> = emptyMap()
    ) {
        try {
            val analytics = firebaseAnalytics ?: return

            // Create bundle with parameters
            val bundle = Bundle().apply {
                putString("message", message.take(100)) // Firebase max 100 chars per value

                // Add custom params
                params.forEach { (key, value) ->
                    // Firebase key limit: 24 chars, value limit: 100 chars
                    putString(key.take(24), value.take(100))
                }
            }

            // Log event (Firebase limits event name to 40 chars)
            val sanitizedEventName = eventName.take(40).replace(Regex("[^a-zA-Z0-9_]"), "_")
            analytics.logEvent(sanitizedEventName, bundle)

            Log.d(TAG, "Event logged: $sanitizedEventName - $message")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging event: $eventName", e)
        }
    }

    /**
     * Log event with user ID for user-scoped analytics.
     */
    fun logEventWithUser(
        eventName: String,
        message: String,
        userId: String,
        params: Map<String, String> = emptyMap()
    ) {
        try {
            val analytics = firebaseAnalytics ?: return

            // Set user ID
            analytics.setUserId(userId)

            // Log event with params including user context
            val allParams = params.toMutableMap().apply {
                put("user_id", userId)
            }

            logEvent(eventName, message, allParams)
        } catch (e: Exception) {
            Log.e(TAG, "Error logging event with user: $eventName", e)
        }
    }

    /**
     * Clear user ID (e.g., on logout).
     */
    fun clearUserId() {
        try {
            firebaseAnalytics?.setUserId(null)
            Log.d(TAG, "User ID cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user ID", e)
        }
    }
}
