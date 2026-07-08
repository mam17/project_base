package com.example.myapplication.base_ads.utils

import android.content.Context
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.ResponseInfo

object MmpAdEventTracker {

    fun logImpression(
        context: Context,
        adType: String,
        placement: String,
        responseInfo: ResponseInfo?
    ) {
        val parameters = buildParameters(adType, placement, responseInfo)
        logEvent(context, AFInAppEventType.AD_VIEW, parameters)
        logEvent(context, AFInAppEventType.CONTENT_VIEW, parameters)
    }

    fun logClick(
        context: Context,
        adType: String,
        placement: String,
        responseInfo: ResponseInfo?
    ) {
        logEvent(
            context,
            AFInAppEventType.AD_CLICK,
            buildParameters(adType, placement, responseInfo)
        )
    }

    private fun logEvent(context: Context, eventName: String, parameters: Map<String, Any>) {
        AppsFlyerLib.getInstance().logEvent(context.applicationContext, eventName, parameters)
    }

    private fun buildParameters(
        adType: String,
        placement: String,
        responseInfo: ResponseInfo?
    ): Map<String, Any> {
        val placementId = placement.ifBlank { "unknown_$adType" }
        val networkName = responseInfo?.loadedAdapterResponseInfo?.adSourceName
            ?: responseInfo?.mediationAdapterClassName
            ?: "admob"

        return mapOf(
            AFInAppEventParameterName.CONTENT_ID to placementId,
            AFInAppEventParameterName.CONTENT_TYPE to adType,
            AFInAppEventParameterName.AD_REVENUE_AD_TYPE to adType,
            AFInAppEventParameterName.AD_REVENUE_PLACEMENT_ID to placementId,
            AFInAppEventParameterName.AD_REVENUE_NETWORK_NAME to networkName
        )
    }
}
