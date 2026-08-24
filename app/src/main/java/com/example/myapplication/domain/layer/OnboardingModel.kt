package com.example.myapplication.domain.layer

import com.example.myapplication.ads.AdPlacement

data class OnboardingModel(
    val resImage: Int,
    val resTitle: Int,
    val resDescription: Int,
    val isNativeAd: Boolean = false,
    val nativePlacement: AdPlacement? = null
) {
    companion object {
        const val FULL_NATIVE_FLAG = 1822
    }
}