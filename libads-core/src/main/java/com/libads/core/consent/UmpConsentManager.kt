package com.libads.core.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object UmpConsentManager {
    private const val TAG = "UmpConsentManager"

    private var consentInformation: ConsentInformation? = null
    private var isGatheringConsent = false
    private val pendingCallbacks = mutableListOf<(Boolean) -> Unit>()

    fun canRequestAds(context: Context): Boolean {
        val consentInfo = consentInformation ?: UserMessagingPlatform.getConsentInformation(context)
            .also { consentInformation = it }
        return consentInfo.canRequestAds()
    }

    fun isPrivacyOptionsRequired(context: Context): Boolean {
        val consentInfo = consentInformation ?: UserMessagingPlatform.getConsentInformation(context)
            .also { consentInformation = it }
        return consentInfo.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun gatherConsent(
        activity: Activity,
        forceDebugGeographyEea: Boolean = false,
        debugTestDeviceHashedIds: List<String> = emptyList(),
        onComplete: (canRequestAds: Boolean) -> Unit
    ) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = consentInfo

        if (isGatheringConsent) {
            pendingCallbacks.add(onComplete)
            return
        }

        isGatheringConsent = true
        pendingCallbacks.add(onComplete)

        val paramsBuilder = ConsentRequestParameters.Builder()
        if (forceDebugGeographyEea) {
            val debugSettingsBuilder = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)

            debugTestDeviceHashedIds.forEach { hashedId ->
                debugSettingsBuilder.addTestDeviceHashedId(hashedId)
            }

            paramsBuilder.setConsentDebugSettings(debugSettingsBuilder.build())
        }

        consentInfo.requestConsentInfoUpdate(
            activity,
            paramsBuilder.build(),
            {
                Log.d(
                    TAG,
                    "consent updated: status=${consentInfo.consentStatus}, canRequestAds=${consentInfo.canRequestAds()}, privacyOptions=${consentInfo.privacyOptionsRequirementStatus}"
                )
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.e(TAG, "consent form error: ${formError.errorCode} ${formError.message}")
                    }
                    completeGathering(consentInfo.canRequestAds())
                }
            },
            { requestError ->
                Log.e(TAG, "consent update error: ${requestError.errorCode} ${requestError.message}")
                completeGathering(consentInfo.canRequestAds())
            }
        )
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onComplete: (canRequestAds: Boolean) -> Unit = {}
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.e(TAG, "privacy options error: ${formError.errorCode} ${formError.message}")
            }
            onComplete(canRequestAds(activity))
        }
    }

    private fun completeGathering(canRequestAds: Boolean) {
        isGatheringConsent = false
        val callbacks = pendingCallbacks.toList()
        pendingCallbacks.clear()
        callbacks.forEach { callback -> callback(canRequestAds) }
    }
}
