package com.example.myapplication.ads

import com.example.myapplication.BuildConfig
import com.libads.core.AdType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        val defaultJson = RemoteAdConfig.loadDefaultConfigJson()
        assertTrue("Expected default JSON to not be blank", defaultJson.isNotBlank())
        val parsedMap = RemoteAdConfig.parseAdConfigMap(defaultJson)
        val parsedObj = RemoteAdConfig.parseAdConfigObject(defaultJson)

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
    fun `debug build uses AdUnits test IDs for ad units`() {
        if (BuildConfig.DEBUG) {
            val interUnit = RemoteAdConfig.getAdUnit("test_placement", AdType.INTERSTITIAL, is2Floor = false)
            assertNotNull(interUnit)
            assertEquals("test_placement", interUnit?.id)
            assertEquals(AdUnits.TEST_INTERSTITIAL_ID, interUnit?.networkAdUnitId)

            val inter2fUnit = RemoteAdConfig.getAdUnit("test_placement", AdType.INTERSTITIAL, is2Floor = true)
            assertNotNull(inter2fUnit)
            assertEquals("test_placement_2f", inter2fUnit?.id)
            assertEquals(AdUnits.TEST_INTERSTITIAL_ID, inter2fUnit?.networkAdUnitId)

            val twoFloorUnits = RemoteAdConfig.getTwoFloorAdUnits("test_placement", AdType.INTERSTITIAL)
            assertTrue(twoFloorUnits.has2Floor)
            assertTrue(twoFloorUnits.isAvailable)
            assertEquals(AdUnits.TEST_INTERSTITIAL_ID, twoFloorUnits.unit2f?.networkAdUnitId)
            assertEquals(AdUnits.TEST_INTERSTITIAL_ID, twoFloorUnits.unitBase?.networkAdUnitId)
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

    @Test
    fun `AdUnits placement constants match expected JSON placement keys`() {
        assertEquals("inter_splash_first", AdUnits.INTER_SPLASH_FIRST)
        assertEquals("banner_splash_first", AdUnits.BANNER_SPLASH_FIRST)
        assertEquals("native_fs_splash_first", AdUnits.NATIVE_FS_SPLASH_FIRST)
        assertEquals("native_fs_splash_second", AdUnits.NATIVE_FS_SPLASH_SECOND)
        assertEquals("native_language_first_1", AdUnits.NATIVE_LANGUAGE_FIRST_1)
        assertEquals("native_language_first_2", AdUnits.NATIVE_LANGUAGE_FIRST_2)
        assertEquals("native_feature_first", AdUnits.NATIVE_FEATURE_FIRST)
        assertEquals("inter_feature_first", AdUnits.INTER_FEATURE_FIRST)
        assertEquals("reward_feature", AdUnits.REWARD_FEATURE)
        assertEquals("appopen_resume", AdUnits.APPOPEN_RESUME)
        assertEquals("banner_collap", AdUnits.BANNER_COLLAP)
    }

    @Test
    fun `AdUnits helper methods return valid AdUnits and TwoFloorAdUnits`() {
        val twoFloor = AdUnits.getTwoFloor(AdUnits.INTER_FEATURE_FIRST, AdType.INTERSTITIAL)
        assertEquals(AdUnits.INTER_FEATURE_FIRST, twoFloor.placementName)
        assertEquals(AdType.INTERSTITIAL, twoFloor.type)

        val unit = AdUnits.getUnit(AdUnits.NATIVE_FEATURE_FIRST, AdType.NATIVE)
        assertNotNull(unit)
        assertEquals(AdUnits.NATIVE_FEATURE_FIRST, unit?.id)

        assertNotNull(AdUnits.mainInterstitial)
        assertNotNull(AdUnits.mainRewarded)
        assertNotNull(AdUnits.mainRewardedInterstitial)
        assertNotNull(AdUnits.mainBanner)
        assertNotNull(AdUnits.mainNative)
        assertNotNull(AdUnits.appOpenResume)
    }

    @Test
    fun `two floor resolution supports all ad types in debug and release mappings`() {
        val types = listOf(
            AdType.INTERSTITIAL to AdUnits.INTER_FEATURE_FIRST,
            AdType.REWARDED to AdUnits.REWARD_FEATURE,
            AdType.REWARDED_INTERSTITIAL to AdUnits.REWARD_FEATURE,
            AdType.NATIVE to AdUnits.NATIVE_FEATURE_FIRST,
            AdType.BANNER to AdUnits.BANNER_COLLAP,
            AdType.APP_OPEN to AdUnits.APPOPEN_RESUME
        )

        for ((type, placement) in types) {
            val twoFloor = AdUnits.getTwoFloor(placement, type)
            assertEquals(placement, twoFloor.placementName)
            assertEquals(type, twoFloor.type)
            if (BuildConfig.DEBUG) {
                assertTrue("Expected 2F available for $type", twoFloor.isAvailable)
                assertTrue("Expected 2F has 2nd floor in DEBUG for $type", twoFloor.has2Floor)
                assertNotNull(twoFloor.unit2f)
                assertNotNull(twoFloor.unitBase)
            }
        }
    }
}
