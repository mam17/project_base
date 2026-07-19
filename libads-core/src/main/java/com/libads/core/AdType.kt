package com.libads.core

/**
 * Các loại quảng cáo mà lib hỗ trợ.
 * Khi thêm network mới, adapter phải map format của network đó về đúng type này.
 */
enum class AdType {
    BANNER,
    INTERSTITIAL,
    REWARDED,
    REWARDED_INTERSTITIAL,
    NATIVE,
    APP_OPEN
}
