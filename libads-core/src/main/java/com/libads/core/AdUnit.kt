package com.libads.core

/**
 * Đại diện cho một placement quảng cáo cụ thể.
 *
 * @param id id nội bộ do app tự đặt để tra cứu (ví dụ "home_interstitial")
 * @param type loại quảng cáo
 * @param networkAdUnitId id thật của network ads (AdMob unit id, Facebook placement id...)
 * @param providerName tên provider sẽ xử lý ad này, phải khớp với AdProvider.name đã đăng ký
 * @param cacheEnabled có preload/cache sẵn hay load on-demand
 * @param timeoutMillis timeout khi load, tránh treo UI vô thời hạn
 */
data class AdUnit(
    val id: String,
    val type: AdType,
    val networkAdUnitId: String,
    val providerName: String,
    val cacheEnabled: Boolean = true,
    val timeoutMillis: Long = 10_000L,
    val collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
    val nativeLayoutRes: Int = 0
)
