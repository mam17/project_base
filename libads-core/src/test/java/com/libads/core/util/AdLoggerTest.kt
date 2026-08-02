package com.libads.core.util

import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdMediationInfo
import com.libads.core.callback.AdRevenue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AdLoggerTest {

    @After
    fun tearDown() {
        AdLogger.eventListener = null
        AdLogger.enabled = true
    }

    @Test
    fun paidEventIsDeliveredWithPlacementAndRevenueMetadata() {
        AdLogger.enabled = false
        var received: AdEvent? = null
        AdLogger.eventListener = AdEventListener { received = it }
        val revenue = AdRevenue(1_250L, "USD", 3)
        val mediationInfo = AdMediationInfo(
            networkName = "facebook",
            adapterClassName = "com.google.ads.mediation.facebook.FacebookMediationAdapter",
            adSourceName = "Meta Audience Network",
            adSourceInstanceName = "meta_interstitial",
            latencyMillis = 245L
        )

        AdLogger.event(
            adUnit(),
            AdEventType.PAID,
            revenue = revenue,
            mediationInfo = mediationInfo
        )

        assertEquals(AdEventType.PAID, received?.eventType)
        assertEquals("inter_home", received?.adName)
        assertEquals(AdType.INTERSTITIAL, received?.adType)
        assertEquals("admob", received?.providerName)
        assertSame(revenue, received?.revenue)
        assertSame(mediationInfo, received?.mediationInfo)
    }

    private fun adUnit() = AdUnit(
        id = "inter_home",
        type = AdType.INTERSTITIAL,
        networkAdUnitId = "network-id",
        providerName = "admob"
    )
}
