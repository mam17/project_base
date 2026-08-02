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
    fun normalized(maxTwoFloorIds: Int = MAX_TWO_FLOOR_IDS): RemoteAdConfig {
        val normalizedId = id.trim()
        return copy(
            id = normalizedId,
            listId2f = listId2f
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .filterNot { it == normalizedId }
                .distinct()
                .take(maxTwoFloorIds)
                .toList()
        )
    }

    fun isValid(): Boolean = !enabled || id.isNotBlank()

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
        return normalized().listId2f
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapIndexed { index, networkAdUnitId ->
                "${adName}_${index + 2}f" to networkAdUnitId
            }
            .toMap()
    }

    companion object {
        const val MAX_TWO_FLOOR_IDS = 3
    }
}
