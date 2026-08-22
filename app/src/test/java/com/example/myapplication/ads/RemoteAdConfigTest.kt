package com.example.myapplication.ads

import com.example.myapplication.BuildConfig
import com.libads.core.AdType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAdConfigTest {

    @Test
    fun `RemoteAdConfig updates config correctly from JSON`() {
        val testJson = """{
            "test_inter": {
                "enabled": true,
                "id": "ca-app-pub-1234/test_id",
                "id_2f": "ca-app-pub-1234/test_id_2f"
            },
            "test_disabled": {
                "enabled": false,
                "id": "ca-app-pub-1234/disabled_id",
                "id_2f": ""
            }
        }"""

        RemoteAdConfig.updateConfig(testJson, enableAllAds = true)

        val enabledConfig = RemoteAdConfig.getAdUnitConfig("test_inter")
        assertNotNull(enabledConfig)
        assertEquals("ca-app-pub-1234/test_id", enabledConfig?.id)
        assertEquals("ca-app-pub-1234/test_id_2f", enabledConfig?.id_2f)

        val disabledConfig = RemoteAdConfig.getAdUnitConfig("test_disabled")
        assertNull(disabledConfig)
    }

    @Test
    fun `RemoteAdConfig returns null when enableAllAds is false`() {
        val defaultJson = RemoteAdConfig.loadDefaultConfigJson()
        RemoteAdConfig.updateConfig(defaultJson, enableAllAds = false)

        val unit = RemoteAdConfig.getAdUnit("inter_splash_first", AdType.INTERSTITIAL)
        assertNull(unit)

        val twoFloor = RemoteAdConfig.getTwoFloorAdUnits("inter_splash_first", AdType.INTERSTITIAL)
        assertNull(twoFloor.unit2f)
        assertNull(twoFloor.unitBase)

        // Restore
        RemoteAdConfig.updateConfig(defaultJson, enableAllAds = true)
    }
}
