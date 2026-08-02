package com.libads.core.internal

import com.libads.core.AdType
import com.libads.core.AdUnit

internal data class AdKey(
    val adName: String,
    val type: AdType,
    val providerName: String,
    val networkAdUnitId: String
) {
    companion object {
        fun from(adUnit: AdUnit): AdKey = AdKey(
            adName = adUnit.id,
            type = adUnit.type,
            providerName = adUnit.providerName,
            networkAdUnitId = adUnit.networkAdUnitId
        )
    }
}

internal data class PlacementKey(
    val adName: String,
    val type: AdType,
    val providerName: String
) {
    companion object {
        fun from(adUnit: AdUnit): PlacementKey = PlacementKey(
            adName = adUnit.id,
            type = adUnit.type,
            providerName = adUnit.providerName
        )
    }
}
