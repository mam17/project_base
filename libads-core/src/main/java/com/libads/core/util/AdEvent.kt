package com.libads.core.util

import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdMediationInfo
import com.libads.core.callback.AdRevenue

enum class AdEventType {
    LOAD_STARTED,
    LOADED,
    LOAD_FAILED,
    LOAD_TIMED_OUT,
    SHOW_STARTED,
    SHOWN,
    IMPRESSION,
    CLICKED,
    PAID,
    REWARD_EARNED,
    DISMISSED,
    SHOW_FAILED
}

data class AdEvent(
    val eventType: AdEventType,
    val adName: String,
    val adType: AdType,
    val providerName: String,
    val errorCode: Int? = null,
    val message: String? = null,
    val revenue: AdRevenue? = null,
    val mediationInfo: AdMediationInfo? = null,
    val rewardAmount: Int? = null,
    val rewardType: String? = null,
    val durationMillis: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun from(
            adUnit: AdUnit,
            eventType: AdEventType,
            errorCode: Int? = null,
            message: String? = null,
            revenue: AdRevenue? = null,
            mediationInfo: AdMediationInfo? = null,
            rewardAmount: Int? = null,
            rewardType: String? = null,
            durationMillis: Long? = null
        ) = AdEvent(
            eventType = eventType,
            adName = adUnit.id,
            adType = adUnit.type,
            providerName = adUnit.providerName,
            errorCode = errorCode,
            message = message,
            revenue = revenue,
            mediationInfo = mediationInfo,
            rewardAmount = rewardAmount,
            rewardType = rewardType,
            durationMillis = durationMillis
        )
    }
}

fun interface AdEventListener {
    fun onEvent(event: AdEvent)
}
