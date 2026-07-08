package com.example.myapplication.base_ads.utils

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AdUnitConfig(
    @SerializedName("enabled")
    @Expose var enabled: Boolean = true,
    @SerializedName("id")
    @Expose var id: String = "",
    @SerializedName("id_2f")
    @Expose var id_2f: String = ""
)

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

    @SerializedName("reward_feature")
    @Expose var reward_feature: AdUnitConfig = AdUnitConfig(),



)