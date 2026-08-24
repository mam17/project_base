package com.example.myapplication.ads

import androidx.annotation.LayoutRes
import com.libads.core.AdType
import com.libads.core.CollapsiblePositionType
import com.example.myapplication.R

data class AdPlacement(
    val name: String,
    val type: AdType,
    val useTwoFloor: Boolean = true,
    val showLoadingWhenNotReady: Boolean = true,
    val collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
    val nativeType: NativeType = NativeType.DEFAULT,
    @get:LayoutRes val loadingLayoutRes: Int = nativeType.loadingLayoutRes,
    @get:LayoutRes val nativeLayoutRes: Int = nativeType.layoutRes
) {
    companion object {
        // ─── Factory methods ───────────────────────────────────────

        fun interstitial(
            name: String,
            showLoading: Boolean = true,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.INTERSTITIAL,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = showLoading
        )

        fun rewarded(
            name: String,
            showLoading: Boolean = true,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.REWARDED,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = showLoading
        )

        fun rewardedInterstitial(
            name: String,
            showLoading: Boolean = true,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.REWARDED_INTERSTITIAL,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = showLoading
        )

        fun banner(
            name: String,
            collapsible: CollapsiblePositionType = CollapsiblePositionType.NONE,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.BANNER,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = false,
            collapsiblePositionType = collapsible
        )

        fun native(
            name: String,
            nativeType: NativeType = NativeType.DEFAULT,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.NATIVE,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = false,
            nativeType = nativeType,
            loadingLayoutRes = nativeType.loadingLayoutRes,
            nativeLayoutRes = nativeType.layoutRes
        )

        fun native(
            name: String,
            @LayoutRes loadingLayout: Int,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.NATIVE,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = false,
            nativeType = NativeType.DEFAULT,
            loadingLayoutRes = loadingLayout,
            nativeLayoutRes = NativeType.DEFAULT.layoutRes
        )

        fun native(
            name: String,
            @LayoutRes nativeLayoutRes: Int,
            @LayoutRes loadingLayoutRes: Int,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.NATIVE,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = false,
            nativeType = NativeType.DEFAULT,
            loadingLayoutRes = loadingLayoutRes,
            nativeLayoutRes = nativeLayoutRes
        )

        fun appOpen(
            name: String,
            useTwoFloor: Boolean = true
        ) = AdPlacement(
            name = name,
            type = AdType.APP_OPEN,
            useTwoFloor = useTwoFloor,
            showLoadingWhenNotReady = false
        )

        // ─── Predefined placements ────────────────────────────────

        val INTER_FEATURE get() = interstitial(AdUnits.INTER_FEATURE_FIRST)
        val INTER_HOME get() = interstitial(AdUnits.INTER_HOME)
        val INTER_BACK get() = interstitial(AdUnits.INTER_BACK)
        val INTER_SPLASH get() = interstitial(AdUnits.INTER_SPLASH_FIRST)

        val REWARD get() = rewarded(AdUnits.REWARD_FEATURE)
        val REWARD_INTER get() = rewardedInterstitial(AdUnits.REWARD_FEATURE)

        val NATIVE_FEATURE get() = native(AdUnits.NATIVE_FEATURE_FIRST, NativeType.TYPE_1)
        val NATIVE_LANGUAGE_1 get() = native(AdUnits.NATIVE_LANGUAGE_FIRST_1, NativeType.TYPE_3)
        val NATIVE_LANGUAGE_2 get() = native(AdUnits.NATIVE_LANGUAGE_FIRST_2, NativeType.TYPE_4)
        val NATIVE_OB_1 get() = native(AdUnits.NATIVE_OB_FIRST_1, NativeType.TYPE_2)
        val NATIVE_OB_3 get() = native(AdUnits.NATIVE_OB_FIRST_3, NativeType.TYPE_3)
        val NATIVE_FS_SPLASH get() = native(AdUnits.NATIVE_FS_SPLASH_FIRST, NativeType.FULL_SCREEN)

        val BANNER_COLLAP get() = banner(AdUnits.BANNER_COLLAP)
        val APP_OPEN_RESUME get() = appOpen(AdUnits.APPOPEN_RESUME)
    }
}
