package com.example.myapplication.ads

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.libads.core.AdType
import com.libads.core.AdUnit

data class AdUnitConfig(
    @SerializedName("enabled")
    @Expose var enabled: Boolean = true,
    @SerializedName("id")
    @Expose var id: String = "",
    @SerializedName("id_2f")
    @Expose var id_2f: String = ""
) {
    fun has2Floor(): Boolean = id_2f.isNotBlank()

    fun isValid(): Boolean = !enabled || id.isNotBlank() || id_2f.isNotBlank()

    fun normalized(): AdUnitConfig = copy(
        id = id.trim(),
        id_2f = id_2f.trim()
    )
}

data class TwoFloorAdUnits(
    val placementName: String,
    val type: AdType,
    val unit2f: AdUnit? = null,
    val unitBase: AdUnit? = null
) {
    val has2Floor: Boolean get() = unit2f != null
    val isAvailable: Boolean get() = unit2f != null || unitBase != null
}

data class AdConfig(

    //Splash
    @SerializedName("inter_splash_first")
    @Expose var inter_splash_first: AdUnitConfig = AdUnitConfig(),
    @SerializedName("banner_splash_first")
    @Expose var banner_splash_first: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_fs_splash_first")
    @Expose var native_fs_splash_first: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_fs_splash_second")
    @Expose var native_fs_splash_second: AdUnitConfig = AdUnitConfig(),

    //Language
    @SerializedName("native_language_first_1")
    @Expose var native_language_first_1: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_language_first_2")
    @Expose var native_language_first_2: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_language_second_1")
    @Expose var native_language_second_1: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_language_second_2")
    @Expose var native_language_second_2: AdUnitConfig = AdUnitConfig(),

    //Feature
    @SerializedName("native_feature_first")
    @Expose var native_feature_first: AdUnitConfig = AdUnitConfig(),
    @SerializedName("inter_feature_first")
    @Expose var inter_feature_first: AdUnitConfig = AdUnitConfig(),

    //Onboarding
    @SerializedName("native_ob_first_1")
    @Expose var native_ob_first_1: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_ob_first_3")
    @Expose var native_ob_first_3: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_fs_first_1")
    @Expose var native_fs_first_1: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_fs_first_2")
    @Expose var native_fs_first_2: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_ob_second_1")
    @Expose var native_ob_second_1: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_ob_second_3")
    @Expose var native_ob_second_3: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_fs_second_1")
    @Expose var native_fs_second_1: AdUnitConfig = AdUnitConfig(),
    @SerializedName("native_fs_second_2")
    @Expose var native_fs_second_2: AdUnitConfig = AdUnitConfig(),

    //Open Resume
    @SerializedName("appopen_resume")
    @Expose var appopen_resume: AdUnitConfig = AdUnitConfig(),

    //Home
    @SerializedName("inter_home")
    @Expose var inter_home: AdUnitConfig = AdUnitConfig(),
    @SerializedName("inter_back")
    @Expose var inter_back: AdUnitConfig = AdUnitConfig(),

    //Reward
    @SerializedName("reward_feature")
    @Expose var reward_feature: AdUnitConfig = AdUnitConfig(),

    //Uninstall & Noti
    @SerializedName("inter_uninstall_openapp")
    @Expose var inter_uninstall_openapp: AdUnitConfig = AdUnitConfig(),
    @SerializedName("inter_still_uninstall")
    @Expose var inter_still_uninstall: AdUnitConfig = AdUnitConfig(),
    @SerializedName("inter_noti_lockscreen")
    @Expose var inter_noti_lockscreen: AdUnitConfig = AdUnitConfig(),

    //Banner
    @SerializedName("banner_collap")
    @Expose var banner_collap: AdUnitConfig = AdUnitConfig()
) {
    fun toMap(): Map<String, AdUnitConfig> = mapOf(
        "inter_splash_first" to inter_splash_first,
        "banner_splash_first" to banner_splash_first,
        "native_fs_splash_first" to native_fs_splash_first,
        "native_fs_splash_second" to native_fs_splash_second,
        "native_language_first_1" to native_language_first_1,
        "native_language_first_2" to native_language_first_2,
        "native_language_second_1" to native_language_second_1,
        "native_language_second_2" to native_language_second_2,
        "native_feature_first" to native_feature_first,
        "inter_feature_first" to inter_feature_first,
        "native_ob_first_1" to native_ob_first_1,
        "native_ob_first_3" to native_ob_first_3,
        "native_fs_first_1" to native_fs_first_1,
        "native_fs_first_2" to native_fs_first_2,
        "native_ob_second_1" to native_ob_second_1,
        "native_ob_second_3" to native_ob_second_3,
        "native_fs_second_1" to native_fs_second_1,
        "native_fs_second_2" to native_fs_second_2,
        "appopen_resume" to appopen_resume,
        "inter_home" to inter_home,
        "inter_back" to inter_back,
        "reward_feature" to reward_feature,
        "inter_uninstall_openapp" to inter_uninstall_openapp,
        "inter_still_uninstall" to inter_still_uninstall,
        "inter_noti_lockscreen" to inter_noti_lockscreen,
        "banner_collap" to banner_collap
    )
}