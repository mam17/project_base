package com.example.myapplication.ads

import androidx.annotation.LayoutRes
import com.example.myapplication.R

enum class NativeType(
    @get:LayoutRes val layoutRes: Int,
    @get:LayoutRes val loadingLayoutRes: Int
) {
    TYPE_1(
        layoutRes = R.layout.layout_native_ad_type1,
        loadingLayoutRes = R.layout.layout_native_loading_type1
    ),

    TYPE_2(
        layoutRes = R.layout.layout_native_ad_type2,
        loadingLayoutRes = R.layout.layout_native_loading_type2
    ),

    TYPE_3(
        layoutRes = R.layout.layout_native_ad_type3,
        loadingLayoutRes = R.layout.layout_native_loading_type3
    ),

    TYPE_4(
        layoutRes = R.layout.layout_native_ad_type4,
        loadingLayoutRes = R.layout.layout_native_loading_type4
    ),

    FULL_SCREEN(
        layoutRes = R.layout.layout_native_ad_full_screen,
        loadingLayoutRes = R.layout.layout_native_fs_loading
    );

    companion object {
        val DEFAULT = TYPE_1

        fun fromKey(key: String?): NativeType = when (key?.lowercase()?.trim()) {
            "type_1", "type1", "1" -> TYPE_1
            "type_2", "type2", "2" -> TYPE_2
            "type_3", "type3", "3" -> TYPE_3
            "type_4", "type4", "4" -> TYPE_4
            "fs", "full_screen", "fullscreen" -> FULL_SCREEN
            else -> DEFAULT
        }
    }
}
