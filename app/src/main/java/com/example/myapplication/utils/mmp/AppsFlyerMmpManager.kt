package com.example.myapplication.utils.mmp

import android.app.Activity
import android.app.Application
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.AdRevenueScheme
import com.appsflyer.share.MediationNetwork
import com.appsflyer.share.attribution.AppsFlyerRequestListener
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.libads.core.util.AdEvent
import com.libads.core.util.AdEventType
import java.util.ArrayDeque

object AppsFlyerMmpManager {
    private const val TAG = "AppsFlyerMmp"
    private const val MICROS_PER_CURRENCY_UNIT = 1_000_000.0
    private const val MAX_PENDING_REVENUE_EVENTS = 50

    private val lock = Any()
    private val pendingRevenueEvents = ArrayDeque<AdEvent>()
    private var initialized = false
    private var sdkReady = false
    private var launcherReady = false
    private var consentReady = false
    private var startInvoked = false

    fun initialize(application: Application) {
        val devKey = application.getString(R.string.appsflyer_key).trim()
        if (devKey.isEmpty()) {
            Log.e(TAG, "AppsFlyer dev key is empty; MMP initialization skipped")
            return
        }

        synchronized(lock) {
            if (initialized) return
            initialized = true
        }

        runCatching {
            AppsFlyerLib.getInstance().apply {
                setDebugLog(BuildConfig.DEBUG)
                enableTCFDataCollection(true)
                init(devKey, null, application)
                registerSessionReadyListener {
                    synchronized(lock) { sdkReady = true }
                    startIfReady()
                }
            }
        }.onFailure { error ->
            synchronized(lock) { initialized = false }
            Log.e(TAG, "AppsFlyer initialization failed", error)
        }
    }

    fun onLauncherActivityCreated(activity: Activity) {
        runCatching {
            AppsFlyerLib.getInstance().collectDataFromLauncherActivity(activity)
        }.onFailure { error ->
            Log.e(TAG, "Failed to collect launcher attribution data", error)
        }
        synchronized(lock) { launcherReady = true }
        startIfReady()
    }

    fun onConsentReady() {
        synchronized(lock) { consentReady = true }
        startIfReady()
    }

    fun trackAdEvent(event: AdEvent) {
        if (event.eventType != AdEventType.PAID || event.revenue == null) return

        val sendImmediately = synchronized(lock) {
            if (startInvoked) {
                true
            } else {
                if (pendingRevenueEvents.size >= MAX_PENDING_REVENUE_EVENTS) {
                    pendingRevenueEvents.removeFirst()
                }
                pendingRevenueEvents.addLast(event)
                false
            }
        }
        if (sendImmediately) sendAdRevenue(event)
    }

    private fun startIfReady() {
        val shouldStart = synchronized(lock) {
            if (!initialized || !sdkReady || !launcherReady || !consentReady || startInvoked) {
                false
            } else {
                startInvoked = true
                true
            }
        }
        if (!shouldStart) return

        AppsFlyerLib.getInstance().start(object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(TAG, "AppsFlyer launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                Log.e(TAG, "AppsFlyer launch failed: $errorCode $errorDesc")
            }
        })

        val queuedEvents = synchronized(lock) {
            pendingRevenueEvents.toList().also { pendingRevenueEvents.clear() }
        }
        queuedEvents.forEach(::sendAdRevenue)
    }

    private fun sendAdRevenue(event: AdEvent) {
        val revenue = event.revenue ?: return
        val mediation = event.mediationInfo
        val monetizationNetwork = mediation?.adSourceName
            ?.takeIf { it.isNotBlank() }
            ?: mediation?.networkName
            ?: event.providerName
        val revenueData = AFAdRevenueData(
            monetizationNetwork,
            MediationNetwork.GOOGLE_ADMOB,
            revenue.currencyCode.uppercase(),
            revenue.valueMicros / MICROS_PER_CURRENCY_UNIT
        )
        if (!revenueData.areAllFieldsValid()) {
            Log.e(TAG, "Invalid ad revenue data for placement '${event.adName}'")
            return
        }

        val additionalParameters = mutableMapOf<String, Any>(
            AdRevenueScheme.AD_UNIT to event.adName,
            AdRevenueScheme.AD_TYPE to event.adType.name.lowercase(),
            AdRevenueScheme.PLACEMENT to event.adName,
            "precision_type" to revenue.precisionType
        )
        mediation?.adapterClassName?.let { additionalParameters["adapter_class"] = it }
        mediation?.adSourceId?.let { additionalParameters["ad_source_id"] = it }
        mediation?.adSourceInstanceName?.let {
            additionalParameters["ad_source_instance_name"] = it
        }
        mediation?.adSourceInstanceId?.let {
            additionalParameters["ad_source_instance_id"] = it
        }
        mediation?.latencyMillis?.let { additionalParameters["mediation_latency_ms"] = it }

        runCatching {
            AppsFlyerLib.getInstance().logAdRevenue(revenueData, additionalParameters)
        }.onSuccess {
            Log.d(
                TAG,
                "Ad revenue logged placement='${event.adName}' network='$monetizationNetwork'"
            )
        }.onFailure { error ->
            Log.e(TAG, "Failed to log ad revenue for '${event.adName}'", error)
        }
    }
}
