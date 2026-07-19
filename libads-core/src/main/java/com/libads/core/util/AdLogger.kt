package com.libads.core.util

import android.util.Log

/**
 * Logger riêng của lib. App có thể tắt hoàn toàn bằng AdLogger.enabled = false
 * (nên tắt ở bản release để tránh lộ thông tin debug/log rác).
 */
object AdLogger {
    private const val TAG = "LibAds"
    var enabled: Boolean = true

    fun d(msg: String) {
        if (enabled) Log.d(TAG, msg)
    }

    fun w(msg: String) {
        if (enabled) Log.w(TAG, msg)
    }

    fun e(msg: String, throwable: Throwable? = null) {
        if (enabled) Log.e(TAG, msg, throwable)
    }
}
