package com.libads.core.internal

import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdCacheTest {

    @Test
    fun concurrentPreloadsJoinOneRequestAndReceiveOneResult() {
        val cache = AdCache()
        val adUnit = adUnit()
        val firstResults = mutableListOf<AdResult>()
        val secondResults = mutableListOf<AdResult>()

        val started = cache.register(adUnit) { firstResults += it }
        val joined = cache.register(adUnit) { secondResults += it }

        assertTrue(started is AdCache.Registration.Started)
        assertEquals(AdCache.Registration.Joined, joined)

        cache.complete(
            adUnit,
            (started as AdCache.Registration.Started).requestId,
            AdResult.Success(adUnit.id)
        )

        assertEquals(1, firstResults.size)
        assertEquals(1, secondResults.size)
        assertTrue(firstResults.single() is AdResult.Success)
        assertTrue(secondResults.single() is AdResult.Success)
    }

    @Test
    fun staleCompletionCannotDispatchCallbacksFromANewerRequest() {
        val cache = AdCache()
        val adUnit = adUnit()
        val oldResults = mutableListOf<AdResult>()
        val newResults = mutableListOf<AdResult>()

        val oldRequest = cache.register(adUnit) { oldResults += it } as AdCache.Registration.Started
        cache.cancel(adUnit)
        val newRequest = cache.register(adUnit) { newResults += it } as AdCache.Registration.Started

        cache.complete(adUnit, oldRequest.requestId, AdResult.Success(adUnit.id))
        assertTrue(newResults.isEmpty())

        cache.complete(adUnit, newRequest.requestId, AdResult.Success(adUnit.id))
        assertEquals(1, newResults.size)
        assertTrue(oldResults.isEmpty())
    }

    @Test
    fun networkIdIsPartOfTheLoadingKey() {
        val cache = AdCache()
        val first = adUnit(networkId = "network-a")
        val second = adUnit(networkId = "network-b")

        assertTrue(cache.register(first) is AdCache.Registration.Started)
        assertTrue(cache.register(second) is AdCache.Registration.Started)
    }

    private fun adUnit(networkId: String = "network-id") = AdUnit(
        id = "inter_home",
        type = AdType.INTERSTITIAL,
        networkAdUnitId = networkId,
        providerName = "fake"
    )
}
