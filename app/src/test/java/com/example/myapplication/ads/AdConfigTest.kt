package com.example.myapplication.ads

import com.example.myapplication.BuildConfig
import com.example.myapplication.utils.firebase.FirebaseConfigManager
import com.libads.core.AdType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdConfigTest {

    @Test
    fun `ad unit config normalization trims ids`() {
        val config = AdUnitConfig(
            enabled = true,
            id = " ca-app-pub-123 ",
            id_2f = " ca-app-pub-456 "
        ).normalized()

        assertEquals("ca-app-pub-123", config.id)
        assertEquals("ca-app-pub-456", config.id_2f)
        assertTrue(config.has2Floor())
        assertTrue(config.isValid())
    }

    @Test
    fun `ad unit config isValid returns false only when enabled and both ids are blank`() {
        val invalidConfig = AdUnitConfig(enabled = true, id = "", id_2f = "")
        assertFalse(invalidConfig.isValid())

        val disabledConfig = AdUnitConfig(enabled = false, id = "", id_2f = "")
        assertTrue(disabledConfig.isValid())

        val only2fConfig = AdUnitConfig(enabled = true, id = "", id_2f = "ca-app-pub-2f")
        assertTrue(only2fConfig.isValid())
    }

    @Test
    fun `parse default ad config json successfully parses all standard placements`() {
        val manager = FirebaseConfigManager.instance()
        val parsedMap = manager.parseAdConfigMap(FirebaseConfigManager.DEFAULT_AD_CONFIG_JSON)
        val parsedObj = manager.parseAdConfigObject(FirebaseConfigManager.DEFAULT_AD_CONFIG_JSON)

        assertNotNull(parsedMap)
        assertNotNull(parsedObj)

        assertTrue(parsedMap!!.containsKey("inter_splash_first"))
        assertEquals("ca-app-pub-1458302844590844/2724162411", parsedMap["inter_splash_first"]?.id)
        assertEquals("ca-app-pub-1458302844590844/1411080749", parsedMap["inter_splash_first"]?.id_2f)

        assertTrue(parsedMap.containsKey("native_feature_first"))
        assertEquals("ca-app-pub-1458302844590844/1209147609", parsedMap["native_feature_first"]?.id)

        assertEquals("ca-app-pub-1458302844590844/2724162411", parsedObj!!.inter_splash_first.id)
    }

    @Test
    fun `debug build uses BuildConfig test IDs for ad units`() {
        val manager = FirebaseConfigManager.instance()

        if (BuildConfig.DEBUG) {
            val interUnit = manager.getAdUnit("test_placement", AdType.INTERSTITIAL, is2Floor = false)
            assertNotNull(interUnit)
            assertEquals("test_placement", interUnit?.id)
            assertEquals(BuildConfig.inter_test, interUnit?.networkAdUnitId)

            val inter2fUnit = manager.getAdUnit("test_placement", AdType.INTERSTITIAL, is2Floor = true)
            assertNotNull(inter2fUnit)
            assertEquals("test_placement_2f", inter2fUnit?.id)
            assertEquals(BuildConfig.inter_test, inter2fUnit?.networkAdUnitId)

            val twoFloorUnits = manager.getTwoFloorAdUnits("test_placement", AdType.INTERSTITIAL)
            assertTrue(twoFloorUnits.has2Floor)
            assertTrue(twoFloorUnits.isAvailable)
            assertEquals(BuildConfig.inter_test, twoFloorUnits.unit2f?.networkAdUnitId)
            assertEquals(BuildConfig.inter_test, twoFloorUnits.unitBase?.networkAdUnitId)
        }
    }

    @Test
    fun `two floor ad units helper methods identify 2F availability`() {
        val noUnits = TwoFloorAdUnits("placement", AdType.INTERSTITIAL, null, null)
        assertFalse(noUnits.has2Floor)
        assertFalse(noUnits.isAvailable)

        val baseOnly = TwoFloorAdUnits(
            placementName = "placement",
            type = AdType.INTERSTITIAL,
            unit2f = null,
            unitBase = com.libads.core.AdUnit("base", AdType.INTERSTITIAL, "id_base", "admob")
        )
        assertFalse(baseOnly.has2Floor)
        assertTrue(baseOnly.isAvailable)
    }
}
