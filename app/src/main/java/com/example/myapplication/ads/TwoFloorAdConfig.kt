package com.example.myapplication.ads

import com.libads.core.AdUnit

/**
 * Complete configuration for a two-floor placement.
 * The map key is the ad name used for logging/analytics and the value is the
 * network ad unit id. Entries are attempted in insertion order.
 */
data class TwoFloorAdConfig(
    val adUnit: AdUnit,
    val networkAdUnitIds: Map<String, String>,
    val twoFloorTimeoutMillis: Long = 3_000L,
    val normalTimeoutMillis: Long = adUnit.timeoutMillis
) {
    init {
        require(twoFloorTimeoutMillis > 0) { "twoFloorTimeoutMillis must be greater than 0" }
        require(normalTimeoutMillis > 0) { "normalTimeoutMillis must be greater than 0" }
    }

    constructor(
        adUnit: AdUnit,
        networkAdUnitIds: List<String>,
        twoFloorTimeoutMillis: Long = 3_000L,
        normalTimeoutMillis: Long = adUnit.timeoutMillis
    ) : this(
        adUnit = adUnit,
        networkAdUnitIds = networkAdUnitIds.mapIndexed { index, networkAdUnitId ->
            "${adUnit.id}_${index + 2}f" to networkAdUnitId
        }.toMap(),
        twoFloorTimeoutMillis = twoFloorTimeoutMillis,
        normalTimeoutMillis = normalTimeoutMillis
    )
}