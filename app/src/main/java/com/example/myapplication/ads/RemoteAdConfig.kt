package com.example.myapplication.ads

import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.MyApplication
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.firebase.FirebaseConfigManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.provider.admob.AdMobProvider

/**
 * Manages ad placement mapping, parsing ad_config JSON, and resolving AdUnits for both DEBUG and RELEASE builds.
 */
object RemoteAdConfig {
    private const val TAG = "RemoteAdConfig"

    const val DEFAULT_TIMEOUT_2F_MILLIS = 3_000L
    const val DEFAULT_TIMEOUT_BASE_MILLIS = 10_000L

    private val gson = Gson()

    @Volatile
    var isEnableAllAds: Boolean = true
        private set

    @Volatile
    var adConfig: AdConfig = AdConfig()
        private set

    @Volatile
    var adConfigMap: Map<String, AdUnitConfig> = emptyMap()
        private set

    init {
        loadInitialConfig()
    }

    fun init() {
        // Trigger initialization
    }

    /**
     * Reads the default ad_config.json from resources / assets.
     */
    fun loadDefaultConfigJson(): String {
        return runCatching {
            RemoteAdConfig::class.java.classLoader?.getResourceAsStream("ad_config.json")
                ?.bufferedReader()
                ?.use { it.readText() }
        }.getOrNull()
            ?: runCatching {
                RemoteAdConfig::class.java.getResourceAsStream("/ad_config.json")
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()
            ?: runCatching {
                RemoteAdConfig::class.java.getResourceAsStream("ad_config.json")
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()
            ?: runCatching {
                RemoteAdConfig::class.java.getResourceAsStream("/com/example/myapplication/ads/ad_config.json")
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()
            ?: runCatching {
                MyApplication.context?.assets?.open("ad_config.json")
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()
            .orEmpty()
    }

    /**
     * Updates the active config and map from Remote Config JSON.
     */
    fun updateConfig(json: String, enableAllAds: Boolean) {
        isEnableAllAds = enableAllAds
        val parsed = parseAdConfigMap(json)
        if (parsed != null) {
            adConfigMap = parsed
            adConfig = parseAdConfigObject(json) ?: AdConfig()
        }
    }

    fun getAdUnitConfig(adName: String): AdUnitConfig? {
        if (!isEnableAllAds) return null
        val config = adConfigMap[adName] ?: return null
        return if (config.enabled) config else null
    }

    fun getTestAdUnitId(type: AdType): String {
        return when (type) {
            AdType.INTERSTITIAL -> AdUnits.TEST_INTERSTITIAL_ID
            AdType.NATIVE -> AdUnits.TEST_NATIVE_ID
            AdType.REWARDED -> AdUnits.TEST_REWARDED_ID
            AdType.REWARDED_INTERSTITIAL -> AdUnits.TEST_REWARDED_INTERSTITIAL_ID
            AdType.APP_OPEN -> AdUnits.TEST_APPOPEN_RESUME_ID
            AdType.BANNER -> AdUnits.TEST_BANNER_ID
        }
    }

    fun getAdUnit(
        adName: String,
        type: AdType,
        is2Floor: Boolean = false,
        timeoutMillis: Long = DEFAULT_TIMEOUT_BASE_MILLIS
    ): AdUnit? {
        if (!isEnableAllAds) return null

        if (BuildConfig.DEBUG) {
            val testId = getTestAdUnitId(type)
            return if (is2Floor) {
                AdUnit(
                    id = "${adName}_2f",
                    type = type,
                    networkAdUnitId = testId,
                    providerName = AdMobProvider.PROVIDER_NAME,
                    timeoutMillis = DEFAULT_TIMEOUT_2F_MILLIS
                )
            } else {
                AdUnit(
                    id = adName,
                    type = type,
                    networkAdUnitId = testId,
                    providerName = AdMobProvider.PROVIDER_NAME,
                    timeoutMillis = timeoutMillis
                )
            }
        }

        val config = getAdUnitConfig(adName) ?: return null
        return if (is2Floor) {
            if (config.id_2f.isNotBlank()) {
                AdUnit(
                    id = "${adName}_2f",
                    type = type,
                    networkAdUnitId = config.id_2f.trim(),
                    providerName = AdMobProvider.PROVIDER_NAME,
                    timeoutMillis = DEFAULT_TIMEOUT_2F_MILLIS
                )
            } else {
                null
            }
        } else {
            if (config.id.isNotBlank()) {
                AdUnit(
                    id = adName,
                    type = type,
                    networkAdUnitId = config.id.trim(),
                    providerName = AdMobProvider.PROVIDER_NAME,
                    timeoutMillis = timeoutMillis
                )
            } else {
                null
            }
        }
    }

    fun getTwoFloorAdUnits(
        adName: String,
        type: AdType,
        timeout2fMillis: Long = DEFAULT_TIMEOUT_2F_MILLIS,
        timeoutBaseMillis: Long = DEFAULT_TIMEOUT_BASE_MILLIS
    ): TwoFloorAdUnits {
        if (!isEnableAllAds) {
            return TwoFloorAdUnits(placementName = adName, type = type)
        }

        if (BuildConfig.DEBUG) {
            val testId = getTestAdUnitId(type)
            val unit2f = AdUnit(
                id = "${adName}_2f",
                type = type,
                networkAdUnitId = testId,
                providerName = AdMobProvider.PROVIDER_NAME,
                timeoutMillis = timeout2fMillis
            )
            val unitBase = AdUnit(
                id = adName,
                type = type,
                networkAdUnitId = testId,
                providerName = AdMobProvider.PROVIDER_NAME,
                timeoutMillis = timeoutBaseMillis
            )
            return TwoFloorAdUnits(
                placementName = adName,
                type = type,
                unit2f = unit2f,
                unitBase = unitBase
            )
        }

        val config = getAdUnitConfig(adName)
        if (config == null || !config.enabled) {
            return TwoFloorAdUnits(placementName = adName, type = type)
        }

        val unit2f = if (config.id_2f.isNotBlank()) {
            AdUnit(
                id = "${adName}_2f",
                type = type,
                networkAdUnitId = config.id_2f.trim(),
                providerName = AdMobProvider.PROVIDER_NAME,
                timeoutMillis = timeout2fMillis
            )
        } else {
            null
        }

        val unitBase = if (config.id.isNotBlank()) {
            AdUnit(
                id = adName,
                type = type,
                networkAdUnitId = config.id.trim(),
                providerName = AdMobProvider.PROVIDER_NAME,
                timeoutMillis = timeoutBaseMillis
            )
        } else {
            null
        }

        return TwoFloorAdUnits(
            placementName = adName,
            type = type,
            unit2f = unit2f,
            unitBase = unitBase
        )
    }

    fun parseAdConfigMap(json: String): Map<String, AdUnitConfig>? {
        if (json.isBlank() || json == "{}") return null
        val type = object : TypeToken<Map<String, AdUnitConfig>>() {}.type
        return runCatching {
            val parsed = gson.fromJson<Map<String, AdUnitConfig>>(json, type).orEmpty()
            val normalized = parsed.mapNotNull { (rawName, rawConfig) ->
                val adName = rawName.trim()
                val config = rawConfig.normalized()
                when {
                    adName.isEmpty() -> null
                    !config.isValid() -> {
                        Log.w(TAG, "Ignoring placement '$adName' because it is invalid")
                        null
                    }
                    else -> adName to config
                }
            }.toMap()
            normalized.takeIf { it.isNotEmpty() }
        }.onFailure { error ->
            Log.e(TAG, "Invalid ad_config JSON; keeping cached value", error)
        }.getOrNull()
    }

    fun parseAdConfigObject(json: String): AdConfig? {
        if (json.isBlank() || json == "{}") return null
        return runCatching {
            gson.fromJson(json, AdConfig::class.java)
        }.getOrNull()
    }

    private fun loadInitialConfig() {
        val preferences = preferences()
        val enableAllAds = preferences?.getBoolean(FirebaseConfigManager.KEY_ENABLE_ALL_ADS, true) ?: true
        val cachedJson = preferences?.getString(FirebaseConfigManager.KEY_AD_CONFIG, "")?.ifBlank { null }
            ?: loadDefaultConfigJson()
        updateConfig(cachedJson, enableAllAds)
    }

    private fun preferences(): SpManager? {
        val application = MyApplication.instance ?: return null
        return runCatching { application.spManager }.getOrNull()
    }
}
