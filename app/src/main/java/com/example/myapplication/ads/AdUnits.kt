package com.example.myapplication.ads

import com.libads.core.AdType
import com.libads.core.AdUnit

/**
 * Single Source of Truth for all ad placement name constants and resolver helpers.
 *
 * Placement names must match the keys in the remote config JSON ([AdConfig]).
 * Use [AdPlacement] factory methods + [Ads] facade for actual ad operations.
 */
object AdUnits {

    // --- Splash Placements ---
    const val INTER_SPLASH_FIRST = "inter_splash_first"
    const val BANNER_SPLASH_FIRST = "banner_splash_first"
    const val NATIVE_FS_SPLASH_FIRST = "native_fs_splash_first"
    const val NATIVE_FS_SPLASH_SECOND = "native_fs_splash_second"

    // --- Language Placements ---
    const val NATIVE_LANGUAGE_FIRST_1 = "native_language_first_1"
    const val NATIVE_LANGUAGE_FIRST_2 = "native_language_first_2"
    const val NATIVE_LANGUAGE_SECOND_1 = "native_language_second_1"
    const val NATIVE_LANGUAGE_SECOND_2 = "native_language_second_2"

    // --- Onboarding Placements ---
    const val NATIVE_OB_FIRST_1 = "native_ob_first_1"
    const val NATIVE_OB_FIRST_3 = "native_ob_first_3"
    const val NATIVE_FS_FIRST_1 = "native_fs_first_1"
    const val NATIVE_FS_FIRST_2 = "native_fs_first_2"

    const val NATIVE_OB_SECOND_1 = "native_ob_second_1"
    const val NATIVE_OB_SECOND_3 = "native_ob_second_3"
    const val NATIVE_FS_SECOND_1 = "native_fs_second_1"
    const val NATIVE_FS_SECOND_2 = "native_fs_second_2"

    // --- Feature Placements ---
    const val NATIVE_FEATURE_FIRST = "native_feature_first"
    const val INTER_FEATURE_FIRST = "inter_feature_first"
    const val REWARD_FEATURE = "reward_feature"

    // --- Navigation Placements ---
    const val INTER_HOME = "inter_home"
    const val INTER_BACK = "inter_back"

    // --- Open Resume Placement ---
    const val APPOPEN_RESUME = "appopen_resume"

    // --- Banner Placements ---
    const val BANNER_COLLAP = "banner_collap"

    // --- Uninstall & Notification Placements ---
    const val INTER_UNINSTALL_OPENAPP = "inter_uninstall_openapp"
    const val INTER_STILL_UNINSTALL = "inter_still_uninstall"
    const val INTER_NOTI_LOCKSCREEN = "inter_noti_lockscreen"

    // ════════════════════════════════════════════════════════════════
    //  Resolvers — delegates to RemoteAdConfig
    // ════════════════════════════════════════════════════════════════

    /**
     * Resolves a [TwoFloorAdUnits] bundle containing Floor 1 (2F) and Floor 2 (Base) AdUnits.
     * Uses BuildConfig test IDs in DEBUG mode and AdUnitConfig from Remote Config in RELEASE mode.
     */
    fun getTwoFloor(
        placementName: String,
        type: AdType,
        timeout2fMillis: Long = RemoteAdConfig.DEFAULT_TIMEOUT_2F_MILLIS,
        timeoutBaseMillis: Long = RemoteAdConfig.DEFAULT_TIMEOUT_BASE_MILLIS
    ): TwoFloorAdUnits {
        return RemoteAdConfig.getTwoFloorAdUnits(
            adName = placementName,
            type = type,
            timeout2fMillis = timeout2fMillis,
            timeoutBaseMillis = timeoutBaseMillis
        )
    }

    /**
     * Resolves a single [AdUnit] for the given placement and type.
     * Uses BuildConfig test IDs in DEBUG mode and AdUnitConfig from Remote Config in RELEASE mode.
     */
    fun getUnit(
        placementName: String,
        type: AdType,
        is2Floor: Boolean = false,
        timeoutMillis: Long = RemoteAdConfig.DEFAULT_TIMEOUT_BASE_MILLIS
    ): AdUnit? {
        return RemoteAdConfig.getAdUnit(
            adName = placementName,
            type = type,
            is2Floor = is2Floor,
            timeoutMillis = timeoutMillis
        )
    }
}
