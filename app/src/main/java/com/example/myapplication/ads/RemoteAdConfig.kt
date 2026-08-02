package com.example.myapplication.ads

import com.google.gson.annotations.SerializedName
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.provider.admob.AdMobProvider

data class RemoteAdConfig(
    @SerializedName("enabled")
    val enabled: Boolean = true,
    @SerializedName("id")
    val id: String = "",
    @SerializedName("list_id_2f")
    val listId2f: List<String> = emptyList()
) {
    fun toAdUnit(
        adName: String,
        type: AdType,
        providerName: String = AdMobProvider.PROVIDER_NAME
    ): AdUnit? {
        if (!enabled || id.isBlank()) return null
        return AdUnit(
            id = adName,
            type = type,
            networkAdUnitId = id,
            providerName = providerName
        )
    }

    fun twoFloorNamedIds(adName: String): Map<String, String> {
        return listId2f
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapIndexed { index, networkAdUnitId ->
                "${adName}_${index + 2}f" to networkAdUnitId
            }
            .toMap()
    }


}
