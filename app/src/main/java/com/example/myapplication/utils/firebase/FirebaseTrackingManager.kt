package com.example.myapplication.utils.firebase

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.example.myapplication.MyApplication
import com.libads.core.util.AdEvent

class FirebaseTrackingManager private constructor() {

    companion object {
        private const val TAG = "FirebaseTrackingManager"
        private const val MAX_EVENT_NAME_LENGTH = 40
        private const val EVENT_NAME_FALLBACK = "app_event"
        private const val MICROS_PER_CURRENCY_UNIT = 1_000_000.0

        const val EVENT_NOTIFICATION_PUSH_SUCCESS = "notification_push_success"
        const val EVENT_NOTIFICATION_CLICK_OPEN_APP = "notification_click_open_app"
        const val EVENT_CLICK_UNINSTALL_APP = "click_uninstall_app"
        const val EVENT_GENERATE_IMAGE_SUCCESS = "generate_image_success"
        const val EVENT_GENERATE_IMAGE_ERROR = "generate_image_error"

        @Volatile
        private var INSTANCE: FirebaseTrackingManager? = null

        fun instance(): FirebaseTrackingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseTrackingManager().also {
                    INSTANCE = it
                }
            }
        }
    }

    private val analytics: FirebaseAnalytics? by lazy {
        try {
            MyApplication.context?.let { FirebaseAnalytics.getInstance(it) }
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAnalytics not initialized: ${e.message}")
            null
        }
    }

    fun logEvent(key: String) {
        logEvent(key, null)
    }

    fun logEvent(key: String, params: Bundle?) {
        val eventName = normalizeEventName(key)
        val firebaseAnalytics = analytics
        if (firebaseAnalytics == null) {
            Log.e(TAG, "FirebaseAnalytics is not ready")
            return
        }

        firebaseAnalytics.logEvent(eventName, params)
        Log.d(TAG, "Logged event: $eventName")
    }

    fun logEvent(key: String, params: Map<String, Any?>) {
        logEvent(key, params.toFirebaseBundle())
    }

    fun logAdEvent(event: AdEvent) {
        val params = linkedMapOf<String, Any?>(
            "ad_name" to event.adName,
            "ad_type" to event.adType.name.lowercase(),
            "ad_provider" to event.providerName,
            "event_time_ms" to event.timestampMillis,
            "error_code" to event.errorCode,
            "error_message" to event.message,
            "reward_amount" to event.rewardAmount,
            "reward_type" to event.rewardType
        )

        event.revenue?.let { revenue ->
            params["revenue_micros"] = revenue.valueMicros
            params["value"] = revenue.valueMicros / MICROS_PER_CURRENCY_UNIT
            params["currency"] = revenue.currencyCode
            params["precision_type"] = revenue.precisionType
        }
        event.mediationInfo?.let { mediation ->
            params["mediation_network"] = mediation.networkName
            params["adapter_class"] = mediation.adapterClassName
            params["ad_source_name"] = mediation.adSourceName
            params["ad_source_id"] = mediation.adSourceId
            params["ad_source_instance_name"] = mediation.adSourceInstanceName
            params["ad_source_instance_id"] = mediation.adSourceInstanceId
            params["mediation_latency_ms"] = mediation.latencyMillis
        }

        logEvent("libads_${event.eventType.name.lowercase()}", params)
    }

    private fun normalizeEventName(key: String): String {
        val cleaned = key
            .trim()
            .replace(Regex("[^A-Za-z0-9_]"), "_")
            .trim('_')
            .take(MAX_EVENT_NAME_LENGTH)

        if (cleaned.isEmpty()) return EVENT_NAME_FALLBACK

        return if (cleaned.first().isLetter()) {
            cleaned
        } else {
            "event_$cleaned".take(MAX_EVENT_NAME_LENGTH)
        }
    }

    private fun Map<String, Any?>.toFirebaseBundle(): Bundle {
        val bundle = Bundle()
        forEach { (key, value) ->
            val paramName = normalizeEventName(key)
            when (value) {
                null -> Unit
                is String -> bundle.putString(paramName, value)
                is Int -> bundle.putLong(paramName, value.toLong())
                is Long -> bundle.putLong(paramName, value)
                is Float -> bundle.putDouble(paramName, value.toDouble())
                is Double -> bundle.putDouble(paramName, value)
                is Boolean -> bundle.putString(paramName, value.toString())
                else -> bundle.putString(paramName, value.toString())
            }
        }
        return bundle
    }
}
