package com.libads.core.util

import android.util.Log
import com.libads.core.AdUnit
import com.libads.core.callback.AdRevenue

/**
 * Logger riêng của lib. App có thể tắt hoàn toàn bằng AdLogger.enabled = false
 * (nên tắt ở bản release để tránh lộ thông tin debug/log rác).
 */
object AdLogger {
    private const val TAG = "LibAds"
    @Volatile
    var enabled: Boolean = true

    @Volatile
    var eventListener: AdEventListener? = null

    fun d(msg: String) {
        if (enabled) Log.d(TAG, msg)
    }

    fun w(msg: String) {
        if (enabled) Log.w(TAG, msg)
    }

    fun e(msg: String, throwable: Throwable? = null) {
        if (enabled) Log.e(TAG, msg, throwable)
    }

    fun event(
        adUnit: AdUnit,
        eventType: AdEventType,
        errorCode: Int? = null,
        message: String? = null,
        revenue: AdRevenue? = null,
        rewardAmount: Int? = null,
        rewardType: String? = null
    ) {
        val event = AdEvent.from(
            adUnit = adUnit,
            eventType = eventType,
            errorCode = errorCode,
            message = message,
            revenue = revenue,
            rewardAmount = rewardAmount,
            rewardType = rewardType
        )
        if (enabled) Log.d(TAG, event.toLogMessage())
        runCatching { eventListener?.onEvent(event) }
            .onFailure { throwable -> e("AdEventListener failed", throwable) }
    }

    private fun AdEvent.toLogMessage(): String = buildString {
        append("event=").append(eventType.name.lowercase())
        append(" adName='").append(adName).append("'")
        append(" adType=").append(adType.name)
        append(" provider='").append(providerName).append("'")
        errorCode?.let { append(" errorCode=").append(it) }
        message?.let { append(" message='").append(it).append("'") }
        revenue?.let {
            append(" valueMicros=").append(it.valueMicros)
            append(" currency=").append(it.currencyCode)
            append(" precision=").append(it.precisionType)
        }
        rewardAmount?.let { append(" rewardAmount=").append(it) }
        rewardType?.let { append(" rewardType='").append(it).append("'") }
    }
}
