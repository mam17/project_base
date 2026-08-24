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
    fun `debug build uses test IDs for ad units`() {
        if (BuildConfig.DEBUG) {
            val testId = RemoteAdConfig.getTestAdUnitId(AdType.INTERSTITIAL)

            val interUnit = RemoteAdConfig.getAdUnit("test_placement", AdType.INTERSTITIAL, is2Floor = false)
            assertNotNull(interUnit)
            assertEquals("test_placement", interUnit?.id)
            assertEquals(testId, interUnit?.networkAdUnitId)

            val inter2fUnit = RemoteAdConfig.getAdUnit("test_placement", AdType.INTERSTITIAL, is2Floor = true)
            assertNotNull(inter2fUnit)
            assertEquals("test_placement_2f", inter2fUnit?.id)
            assertEquals(testId, inter2fUnit?.networkAdUnitId)

            val twoFloorUnits = RemoteAdConfig.getTwoFloorAdUnits("test_placement", AdType.INTERSTITIAL)
            assertTrue(twoFloorUnits.has2Floor)
            assertTrue(twoFloorUnits.isAvailable)
            assertEquals(testId, twoFloorUnits.unit2f?.networkAdUnitId)
            assertEquals(testId, twoFloorUnits.unitBase?.networkAdUnitId)
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
    }

    @Test
    fun `AdPlacement factory methods create correct placements`() {
        val inter = AdPlacement.interstitial(AdUnits.INTER_FEATURE_FIRST)
        assertEquals(AdUnits.INTER_FEATURE_FIRST, inter.name)
        assertEquals(AdType.INTERSTITIAL, inter.type)
        assertTrue(inter.useTwoFloor)
        assertTrue(inter.showLoadingWhenNotReady)

        val reward = AdPlacement.rewarded(AdUnits.REWARD_FEATURE)
        assertEquals(AdType.REWARDED, reward.type)

        val native = AdPlacement.NATIVE_FEATURE
        assertEquals(AdType.NATIVE, native.type)
        assertEquals(AdUnits.NATIVE_FEATURE_FIRST, native.name)
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

    @Test
    fun `NativeType correctly pairs layout and loading resources`() {
        for (nativeType in NativeType.entries) {
            assertTrue("Layout res must be valid for $nativeType", nativeType.layoutRes != 0)
            assertTrue("Loading res must be valid for $nativeType", nativeType.loadingLayoutRes != 0)
        }

        assertEquals(NativeType.TYPE_1, NativeType.fromKey("type_1"))
        assertEquals(NativeType.TYPE_2, NativeType.fromKey("type_2"))
        assertEquals(NativeType.TYPE_3, NativeType.fromKey("type_3"))
        assertEquals(NativeType.TYPE_4, NativeType.fromKey("type_4"))
        assertEquals(NativeType.FULL_SCREEN, NativeType.fromKey("full_screen"))
        assertEquals(NativeType.DEFAULT, NativeType.fromKey("unknown"))
    }

    @Test
    fun `AdPlacement native factory assigns matching native and loading layouts`() {
        val nativeType2 = AdPlacement.native(AdUnits.NATIVE_OB_FIRST_1, NativeType.TYPE_2)
        assertEquals(NativeType.TYPE_2, nativeType2.nativeType)
        assertEquals(NativeType.TYPE_2.layoutRes, nativeType2.nativeLayoutRes)
        assertEquals(NativeType.TYPE_2.loadingLayoutRes, nativeType2.loadingLayoutRes)

        val nativeType3 = AdPlacement.native(AdUnits.NATIVE_LANGUAGE_FIRST_1, NativeType.TYPE_3)
        assertEquals(NativeType.TYPE_3, nativeType3.nativeType)
        assertEquals(NativeType.TYPE_3.layoutRes, nativeType3.nativeLayoutRes)
        assertEquals(NativeType.TYPE_3.loadingLayoutRes, nativeType3.loadingLayoutRes)

        val nativeType4 = AdPlacement.native(AdUnits.NATIVE_LANGUAGE_FIRST_2, NativeType.TYPE_4)
        assertEquals(NativeType.TYPE_4, nativeType4.nativeType)
        assertEquals(NativeType.TYPE_4.layoutRes, nativeType4.nativeLayoutRes)
        assertEquals(NativeType.TYPE_4.loadingLayoutRes, nativeType4.loadingLayoutRes)
    }

    @Test
    fun `Onboarding native placements match required NativeTypes`() {
        val obFirst1 = AdPlacement.native(AdUnits.NATIVE_OB_FIRST_1, NativeType.TYPE_1)
        assertEquals(NativeType.TYPE_1, obFirst1.nativeType)
        assertEquals(NativeType.TYPE_1.layoutRes, obFirst1.nativeLayoutRes)

        val obSecond1 = AdPlacement.native(AdUnits.NATIVE_OB_SECOND_1, NativeType.TYPE_1)
        assertEquals(NativeType.TYPE_1, obSecond1.nativeType)
        assertEquals(NativeType.TYPE_1.layoutRes, obSecond1.nativeLayoutRes)

        val fsFirst1 = AdPlacement.native(AdUnits.NATIVE_FS_FIRST_1, NativeType.FULL_SCREEN)
        assertEquals(NativeType.FULL_SCREEN, fsFirst1.nativeType)
        assertEquals(NativeType.FULL_SCREEN.layoutRes, fsFirst1.nativeLayoutRes)

        val fsFirst2 = AdPlacement.native(AdUnits.NATIVE_FS_FIRST_2, NativeType.FULL_SCREEN)
        assertEquals(NativeType.FULL_SCREEN, fsFirst2.nativeType)
        assertEquals(NativeType.FULL_SCREEN.layoutRes, fsFirst2.nativeLayoutRes)
    }
}
