package com.example.myapplication.utils.firebase

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.MyApplication
import com.example.myapplication.ads.RemoteAdConfig
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

        @Volatile
        private var INSTANCE: FirebaseConfigManager? = null

        fun instance(): FirebaseConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseConfigManager().also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
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
    var adConfig: Map<String, RemoteAdConfig> = emptyMap()
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

    fun getAdConfig(adName: String): RemoteAdConfig? {
        if (!isEnableAllAds) return null
        return adConfig[adName]?.takeIf { it.enabled }
    }

    fun getAdUnit(adName: String, type: AdType): AdUnit? =
        getAdConfig(adName)?.toAdUnit(adName, type)

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
                KEY_AD_CONFIG to "{}",
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
                mainHandler.postDelayed(
                    { performFetch(attempt + 1) },
                    RETRY_DELAY_MS * attempt
                )
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
        parseAdConfig(remoteAdConfigJson)?.let { parsedConfig ->
            adConfig = parsedConfig
            preferences?.putString(KEY_AD_CONFIG, remoteAdConfigJson)
        }

        preferences?.putBoolean(KEY_ENABLE_ALL_ADS, isEnableAllAds)
        preferences?.putBoolean(KEY_ENABLE_FORCE_UPDATE, isEnableForceUpdate)
        preferences?.putString(KEY_NEW_VERSION_NAME, newVersionName)

        Log.i(
            TAG,
            "Remote Config activated: enableAllAds=$isEnableAllAds, " +
                "adPlacements=${adConfig.size}, forceUpdate=$isEnableForceUpdate, " +
                "newVersionName=$newVersionName"
        )
    }

    private fun loadCachedValues() {
        val preferences = preferences() ?: return
        isEnableAllAds = preferences.getBoolean(KEY_ENABLE_ALL_ADS, true)
        isEnableForceUpdate = preferences.getBoolean(KEY_ENABLE_FORCE_UPDATE, false)
        newVersionName = preferences.getString(KEY_NEW_VERSION_NAME, "")?.trim().orEmpty()

        val cachedJson = preferences.getString(KEY_AD_CONFIG, "").orEmpty()
        parseAdConfig(cachedJson)?.let { adConfig = it }
    }

    private fun parseAdConfig(json: String): Map<String, RemoteAdConfig>? {
        if (json.isBlank() || json == "{}") return null
        val type = object : TypeToken<Map<String, RemoteAdConfig>>() {}.type
        return runCatching {
            val parsed = gson.fromJson<Map<String, RemoteAdConfig>>(json, type).orEmpty()
            val normalized = parsed.mapNotNull { (rawName, rawConfig) ->
                val adName = rawName.trim()
                val config = rawConfig.normalized()
                when {
                    adName.isEmpty() -> null
                    !config.isValid() -> {
                        Log.w(TAG, "Ignoring enabled placement '$adName' because id is blank")
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
