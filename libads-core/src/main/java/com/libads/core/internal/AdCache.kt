package com.libads.core.internal

import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import java.util.concurrent.atomic.AtomicLong

internal class AdCache {

    sealed interface Registration {
        data class Started(val requestId: Long) : Registration
        data object Joined : Registration
    }

    private data class LoadingRequest(
        val requestId: Long,
        val callbacks: MutableList<AdLoadCallback>
    )

    private val lock = Any()
    private val nextRequestId = AtomicLong(0L)
    private val loadingRequests = mutableMapOf<AdKey, LoadingRequest>()

    fun register(adUnit: AdUnit, callback: AdLoadCallback? = null): Registration {
        val key = AdKey.from(adUnit)
        synchronized(lock) {
            val current = loadingRequests[key]
            if (current != null) {
                callback?.let(current.callbacks::add)
                return Registration.Joined
            }

            val request = LoadingRequest(
                requestId = nextRequestId.incrementAndGet(),
                callbacks = mutableListOf<AdLoadCallback>().apply {
                    callback?.let(::add)
                }
            )
            loadingRequests[key] = request
            return Registration.Started(request.requestId)
        }
    }

    fun isLoading(adUnit: AdUnit): Boolean = synchronized(lock) {
        loadingRequests.containsKey(AdKey.from(adUnit))
    }

    fun complete(adUnit: AdUnit, requestId: Long, result: AdResult) {
        val callbacks = synchronized(lock) {
            val key = AdKey.from(adUnit)
            val current = loadingRequests[key]
            if (current?.requestId != requestId) return
            loadingRequests.remove(key)
            current.callbacks.toList()
        }
        callbacks.forEach { callback -> callback.onResult(result) }
    }

    fun cancel(adUnit: AdUnit, result: AdResult? = null) {
        val callbacks = synchronized(lock) {
            loadingRequests.remove(AdKey.from(adUnit))?.callbacks?.toList().orEmpty()
        }
        if (result != null) callbacks.forEach { callback -> callback.onResult(result) }
    }

    fun clear(adUnit: AdUnit) {
        cancel(adUnit)
    }

    fun clearAll(resultFactory: ((AdKey) -> AdResult)? = null) {
        val pending = synchronized(lock) {
            val snapshot = loadingRequests.map { (key, request) -> key to request.callbacks.toList() }
            loadingRequests.clear()
            snapshot
        }
        if (resultFactory != null) {
            pending.forEach { (key, callbacks) ->
                val result = resultFactory(key)
                callbacks.forEach { callback -> callback.onResult(result) }
            }
        }
    }
}
