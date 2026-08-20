package com.example.myapplication.utils.firebase

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.MyApplication
import com.example.myapplication.ads.AdConfig
import com.example.myapplication.ads.AdUnitConfig
import com.example.myapplication.ads.TwoFloorAdUnits
import com.example.myapplication.utils.NetworkUtil
import com.example.myapplication.utils.SpManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.libads.core.AdType
import com.libads.core.AdUnit
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseConfigManager private constructor() {

    companion object {
        private const val TAG = "FirebaseConfigManager"
        private const val MAX_FETCH_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1_000L

        const val KEY_AD_CONFIG = "ad_config"
        const val KEY_ENABLE_ALL_ADS = "enable_all_ads"
        const val KEY_ENABLE_FORCE_UPDATE = "enable_force_update"
        const val KEY_NEW_VERSION_NAME = "new_version_name"

        const val DEFAULT_TIMEOUT_2F_MILLIS = 3_000L
        const val DEFAULT_TIMEOUT_BASE_MILLIS = 10_000L

        const val DEFAULT_AD_CONFIG_JSON = """{
  "banner_splash_first": { "enabled": true, "id": "ca-app-pub-1458302844590844/1237866525", "id_2f": "" },
  "inter_splash_first": { "enabled": true, "id": "ca-app-pub-1458302844590844/2724162411", "id_2f": "ca-app-pub-1458302844590844/1411080749" },
  "native_fs_splash_first": { "enabled": true, "id": "ca-app-pub-1458302844590844/7036189356", "id_2f": "ca-app-pub-1458302844590844/1147356862" },
  "native_language_first_1": { "enabled": true, "id": "ca-app-pub-1458302844590844/9798882823", "id_2f": "" },
  "native_language_first_2": { "enabled": true, "id": "ca-app-pub-1458302844590844/7147869996", "id_2f": "ca-app-pub-1458302844590844/1703971627" },
  "native_ob_first_1": { "enabled": true, "id": "ca-app-pub-1458302844590844/1400719298", "id_2f": "ca-app-pub-1458302844590844/9630563106" },
  "native_fs_first_1": { "enabled": true, "id": "ca-app-pub-1458302844590844/7774555956", "id_2f": "ca-app-pub-1458302844590844/5148392618" },
  "native_fs_first_2": { "enabled": true, "id": "ca-app-pub-1458302844590844/2522229275", "id_2f": "ca-app-pub-1458302844590844/2167804819" },
  "native_ob_first_3": { "enabled": true, "id": "", "id_2f": "" },
  "native_feature_first": { "enabled": true, "id": "ca-app-pub-1458302844590844/1209147609", "id_2f": "" },
  "inter_feature_first": { "enabled": true, "id": "ca-app-pub-1458302844590844/5859637816", "id_2f": "ca-app-pub-1458302844590844/9854723145" },
  "appopen_resume": { "enabled": true, "id": "ca-app-pub-1458302844590844/4546556140", "id_2f": "" },
  "native_language_second_1": { "enabled": true, "id": "", "id_2f": "" },
  "native_language_second_2": { "enabled": true, "id": "", "id_2f": "" },
  "native_fs_second_1": { "enabled": true, "id": "", "id_2f": "" },
  "native_fs_second_2": { "enabled": true, "id": "", "id_2f": "" }
}"""

        @Volatile
        private var INSTANCE: FirebaseConfigManager? = null

        fun instance(): FirebaseConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseConfigManager().also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private val mainHandler: Handler? by lazy {
        runCatching { Handler(Looper.getMainLooper()) }.getOrNull()
    }
    private val fetchLock = Any()
    private val pendingFetchCallbacks = mutableListOf<(Boolean) -> Unit>()

    private val remoteConfig: FirebaseRemoteConfig? = runCatching {
        FirebaseRemoteConfig.getInstance()
    }.onFailure { error ->
        Log.e(TAG, "Firebase Remote Config is unavailable", error)
    }.getOrNull()

    @Volatile
    var isEnableAllAds: Boolean = true
        private set

    @Volatile
    var adConfig: AdConfig = AdConfig()
        private set

    @Volatile
    var adConfigMap: Map<String, AdUnitConfig> = emptyMap()
        private set

    @Volatile
    var isEnableForceUpdate: Boolean = false
        private set

    @Volatile
    var newVersionName: String = ""
        private set

    private var isFetching = false

    init {
        loadCachedValues()
        setupRemoteConfig()
    }

    /**
     * Fetches and activates Remote Config. Concurrent callers join the same request.
     */
    fun fetch(onResult: ((Boolean) -> Unit)? = null) {
        var shouldStart = false
        synchronized(fetchLock) {
            onResult?.let(pendingFetchCallbacks::add)
            if (!isFetching) {
                isFetching = true
                shouldStart = true
            }
        }
        if (shouldStart) performFetch(attempt = 1)
    }

    /** Coroutine-friendly version used by SplashActivity with its own timeout. */
    suspend fun fetchAndActivate(): Boolean = suspendCancellableCoroutine { continuation ->
        fetch { success ->
            if (continuation.isActive) {
                continuation.resumeWith(Result.success(success))
            }
        }
    }

    fun getAdUnitConfig(adName: String): AdUnitConfig? {
        if (!isEnableAllAds) return null
        val config = adConfigMap[adName] ?: return null
        return if (config.enabled) config else null
    }

    fun getTestAdUnitId(type: AdType): String {
        return when (type) {
            AdType.INTERSTITIAL -> BuildConfig.inter_test
            AdType.NATIVE -> BuildConfig.native_test
            AdType.REWARDED -> BuildConfig.reward_test
            AdType.REWARDED_INTERSTITIAL -> BuildConfig.reward_inter_test
            AdType.APP_OPEN -> BuildConfig.appopen_resume_test
            AdType.BANNER -> BuildConfig.banner_test
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
                    providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
                    timeoutMillis = DEFAULT_TIMEOUT_2F_MILLIS
                )
            } else {
                AdUnit(
                    id = adName,
                    type = type,
                    networkAdUnitId = testId,
                    providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
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
                    providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
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
                    providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
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
                providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
                timeoutMillis = timeout2fMillis
            )
            val unitBase = AdUnit(
                id = adName,
                type = type,
                networkAdUnitId = testId,
                providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
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
                providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
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
                providerName = com.libads.core.provider.admob.AdMobProvider.PROVIDER_NAME,
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

    fun shouldForceUpdate(currentVersionName: String = BuildConfig.VERSION_NAME): Boolean {
        return isEnableForceUpdate &&
            newVersionName.isNotBlank() &&
            newVersionName != currentVersionName
    }

    fun getString(key: String, defaultValue: String = ""): String =
        remoteConfig?.getString(key)?.ifBlank { defaultValue } ?: defaultValue

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        runCatching { remoteConfig?.getBoolean(key) }.getOrNull() ?: defaultValue

    fun getLong(key: String, defaultValue: Long = 0L): Long =
        runCatching { remoteConfig?.getLong(key) }.getOrNull() ?: defaultValue

    private fun setupRemoteConfig() {
        val config = remoteConfig ?: return
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3_600
            fetchTimeoutInSeconds = 10
        }
        config.setConfigSettingsAsync(settings)
        config.setDefaultsAsync(
            mapOf(
                KEY_ENABLE_ALL_ADS to true,
                KEY_AD_CONFIG to DEFAULT_AD_CONFIG_JSON,
                KEY_ENABLE_FORCE_UPDATE to false,
                KEY_NEW_VERSION_NAME to ""
            )
        )
    }

    private fun performFetch(attempt: Int) {
        val config = remoteConfig
        val context = MyApplication.context
        if (config == null || context == null || !NetworkUtil.isNetworkAvailable(context)) {
            finishFetch(success = false)
            return
        }

        Log.d(TAG, "Fetching Remote Config, attempt=$attempt")
        config.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                applyActivatedValues(config)
                finishFetch(success = true)
                return@addOnCompleteListener
            }

            Log.w(TAG, "Remote Config fetch failed on attempt=$attempt", task.exception)
            if (attempt < MAX_FETCH_ATTEMPTS && NetworkUtil.isNetworkAvailable(context)) {
                mainHandler?.postDelayed(
                    { performFetch(attempt + 1) },
                    RETRY_DELAY_MS * attempt
                ) ?: performFetch(attempt + 1)
            } else {
                finishFetch(success = false)
            }
        }
    }

    private fun applyActivatedValues(config: FirebaseRemoteConfig) {
        val preferences = preferences()

        isEnableAllAds = config.getBoolean(KEY_ENABLE_ALL_ADS)
        isEnableForceUpdate = config.getBoolean(KEY_ENABLE_FORCE_UPDATE)
        newVersionName = config.getString(KEY_NEW_VERSION_NAME).trim()

        val remoteAdConfigJson = config.getString(KEY_AD_CONFIG).trim()
        val parsed = parseAdConfigMap(remoteAdConfigJson)
        if (parsed != null) {
            adConfigMap = parsed
            adConfig = parseAdConfigObject(remoteAdConfigJson) ?: AdConfig()
            preferences?.putString(KEY_AD_CONFIG, remoteAdConfigJson)
        }

        preferences?.putBoolean(KEY_ENABLE_ALL_ADS, isEnableAllAds)
        preferences?.putBoolean(KEY_ENABLE_FORCE_UPDATE, isEnableForceUpdate)
        preferences?.putString(KEY_NEW_VERSION_NAME, newVersionName)

        Log.i(
            TAG,
            "Remote Config activated: enableAllAds=$isEnableAllAds, " +
                "adPlacements=${adConfigMap.size}, forceUpdate=$isEnableForceUpdate, " +
                "newVersionName=$newVersionName"
        )
    }

    private fun loadCachedValues() {
        val preferences = preferences() ?: return
        isEnableAllAds = preferences.getBoolean(KEY_ENABLE_ALL_ADS, true)
        isEnableForceUpdate = preferences.getBoolean(KEY_ENABLE_FORCE_UPDATE, false)
        newVersionName = preferences.getString(KEY_NEW_VERSION_NAME, "")?.trim().orEmpty()

        val cachedJson = preferences.getString(KEY_AD_CONFIG, "").orEmpty()
        val initialJson = cachedJson.ifBlank { DEFAULT_AD_CONFIG_JSON }
        parseAdConfigMap(initialJson)?.let {
            adConfigMap = it
            adConfig = parseAdConfigObject(initialJson) ?: AdConfig()
        }
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
            Log.e(TAG, "Invalid ad_config JSON; keeping the cached value", error)
        }.getOrNull()
    }

    fun parseAdConfigObject(json: String): AdConfig? {
        if (json.isBlank() || json == "{}") return null
        return runCatching {
            gson.fromJson(json, AdConfig::class.java)
        }.getOrNull()
    }

    private fun finishFetch(success: Boolean) {
        val callbacks = synchronized(fetchLock) {
            isFetching = false
            pendingFetchCallbacks.toList().also { pendingFetchCallbacks.clear() }
        }
        callbacks.forEach { callback -> callback(success) }
    }

    private fun preferences(): SpManager? {
        val application = MyApplication.instance ?: return null
        return runCatching { application.spManager }
            .onFailure { error -> Log.w(TAG, "SharedPreferences is not ready", error) }
            .getOrNull()
    }
}
