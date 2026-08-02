package com.libads.core.provider.admob

import org.junit.Assert.assertEquals
import org.junit.Test

class AdMobMediationInfoMapperTest {
    @Test
    fun `maps every configured mediation family`() {
        val adapters = mapOf(
            "facebook.FacebookMediationAdapter" to "facebook",
            "applovin.AppLovinMediationAdapter" to "applovin",
            "vungle.VungleMediationAdapter" to "vungle",
            "pangle.PangleMediationAdapter" to "pangle",
            "mintegral.MintegralMediationAdapter" to "mintegral",
            "inmobi.InMobiMediationAdapter" to "inmobi",
            "ironsource.IronSourceMediationAdapter" to "ironsource",
            "admob.AdMobAdapter" to "admob"
        )

        adapters.forEach { (adapter, expected) ->
            assertEquals(expected, mediationNetworkName(null, adapter))
        }
    }
}
