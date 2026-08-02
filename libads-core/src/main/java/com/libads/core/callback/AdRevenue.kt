package com.libads.core.callback

data class AdRevenue(
    val valueMicros: Long,
    val currencyCode: String,
    val precisionType: Int
)
