package com.libads.core.internal

import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import java.util.concurrent.ConcurrentHashMap

/**
 * Theo dõi trạng thái load theo từng adUnit.id để:
 * - Không gọi load() nhiều lần chồng nhau cho cùng 1 placement
 * - Biết placement nào đang loading / đã sẵn sàng
 *
 * Việc ad thực sự có sẵn hay không vẫn hỏi qua AdProvider.isReady(),
 * cache này chỉ chống race-condition khi gọi load liên tục (ví dụ user bấm nút nhiều lần).
 */
internal class AdCache {

    private val loadingState = ConcurrentHashMap<String, Boolean>()
    private val pendingCallbacks = ConcurrentHashMap<String, MutableList<AdLoadCallback>>()

    fun isLoading(adUnitId: String): Boolean = loadingState[adUnitId] == true

    fun markLoading(adUnitId: String) {
        loadingState[adUnitId] = true
    }

    fun markIdle(adUnitId: String) {
        loadingState[adUnitId] = false
    }

    fun clear(adUnitId: String) {
        loadingState.remove(adUnitId)
        pendingCallbacks.remove(adUnitId)
    }

    fun clearAll() {
        loadingState.clear()
        pendingCallbacks.clear()
    }

    fun addCallback(adUnitId: String, callback: AdLoadCallback?) {
        if (callback == null) return
        pendingCallbacks.getOrPut(adUnitId) { mutableListOf() }.add(callback)
    }

    fun dispatchCallbacks(adUnitId: String, result: AdResult) {
        pendingCallbacks.remove(adUnitId)?.forEach { callback ->
            callback.onResult(result)
        }
    }
}
