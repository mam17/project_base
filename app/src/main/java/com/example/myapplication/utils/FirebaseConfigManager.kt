package com.example.myapplication.utils

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.example.myapplication.BuildConfig
import com.example.myapplication.MyApplication
import com.example.myapplication.base_ads.utils.AdConfig

class FirebaseConfigManager private constructor() {

    companion object {
        private const val TAG = "FirebaseConfigManager"
        private const val MAX_RETRY = 5

        // Remote Config keys
        const val KEY_ADS_CONFIG = "ad_config"
        const val KEY_ENABLE_ALL_ADS = "enable_all_ads"
        const val KEY_ENABLE_FORCE_UPDATE = "enable_force_update"
        const val KEY_NEW_VERSION_NAME = "new_version_name"

        @Volatile
        private var INSTANCE: FirebaseConfigManager? = null

        fun instance(): FirebaseConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseConfigManager().also {
                    INSTANCE = it
                }
            }
        }
    }

    private val remoteConfig: FirebaseRemoteConfig?
        get() = try {
            FirebaseRemoteConfig.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseRemoteConfig not initialized: ${e.message}")
            null
        }

    private val gson = Gson()

    private var retryCount = 0
    private var isFetching = false
    private val pendingFetchCallbacks = mutableListOf<(Boolean) -> Unit>()

    // ========================= PUBLIC CONFIG VALUES =========================

    /** Kill switch toàn bộ ads */
    var isEnableAllAds: Boolean = true
        private set

    /** Ad unit config (parsed from JSON) */
    var adConfig: AdConfig = AdConfig()
        private set

    /** Force update flag */
    var enableForceUpdate: Boolean = false
        private set

    /** New version name from Remote Config */
    var newVersionName: String = ""
        private set

    // ========================= INIT =========================

    init {
        Log.i(TAG, "Initializing FirebaseConfigManager...")
        setup()
        loadLocalAdConfig()
    }

    private fun setup() {
        val config = remoteConfig ?: return
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        config.setConfigSettingsAsync(settings)
        Log.i(TAG, "Firebase RemoteConfig setup complete (DEBUG=${BuildConfig.DEBUG})")
    }

    /**
     * Load ad_config từ local SharedPreferences (cache) để dùng ngay khi app mở,
     * trước khi remote config fetch xong.
     */
    private fun loadLocalAdConfig() {
        val context = MyApplication.context ?: return
        try {
            val sp = SpManager.get(context)
            val json = sp.getString(KEY_ADS_CONFIG, "") ?: ""
            if (json.isNotEmpty()) {
                adConfig = gson.fromJson(json, AdConfig::class.java)
                Log.d(TAG, "Loaded local ad_config cache")
            }

            isEnableAllAds = sp.getBoolean(KEY_ENABLE_ALL_ADS, true)
            enableForceUpdate = sp.getBoolean(KEY_ENABLE_FORCE_UPDATE, false)
            newVersionName = sp.getString(KEY_NEW_VERSION_NAME, "") ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local config: ${e.message}")
        }
    }

    // ========================= FETCH =========================

    fun fetch(onResult: ((Boolean) -> Unit)? = null) {
        Log.i(TAG, "fetch() called, isFetching=$isFetching")
        onResult?.let { pendingFetchCallbacks.add(it) }
        if (isFetching) return

        val config = remoteConfig
        if (config == null) {
            Log.e(TAG, "RemoteConfig is null - fetch aborted")
            notifyFetchResult(false)
            return
        }

        val context = MyApplication.context
        if (context == null) {
            notifyFetchResult(false)
            return
        }

        if (!NetworkUtil.isNetworkAvailable(context)) {
            Log.e(TAG, "No internet - fetch aborted")
            notifyFetchResult(false)
            return
        }

        isFetching = true

        config.fetchAndActivate().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Fetch failed: ${task.exception?.message}")
                retry()
                return@addOnCompleteListener
            }

            Log.i(TAG, "Fetch & activate SUCCESS")
            parseAllRemoteValues()

            retryCount = 0
            notifyFetchResult(true)
        }
    }

    private fun retry() {
        retryCount++
        if (retryCount < MAX_RETRY) {
            isFetching = false
            fetch()
        } else {
            Log.d(TAG, "Retry max reached")
            retryCount = 0
            notifyFetchResult(false)
        }
    }

    private fun notifyFetchResult(success: Boolean) {
        isFetching = false
        val callbacks = pendingFetchCallbacks.toList()
        pendingFetchCallbacks.clear()
        callbacks.forEach { it(success) }
    }

    // ========================= PARSE REMOTE VALUES =========================

    private fun parseAllRemoteValues() {
        val context = MyApplication.context ?: return
        val sp = SpManager.get(context)

        // 1. enable_all_ads
        parseEnableAllAds(sp)

        // 2. ad_config (JSON)
        parseAdConfig(sp)

        // 3. enable_force_update
        parseEnableForceUpdate(sp)

        // 4. new_version_name
        parseNewVersionName(sp)
    }

    private fun parseEnableAllAds(sp: SpManager) {
        try {
            val remoteValue = remoteConfig?.getBoolean(KEY_ENABLE_ALL_ADS) ?: true
            isEnableAllAds = remoteValue
            sp.putBoolean(KEY_ENABLE_ALL_ADS, remoteValue)
            Log.d(TAG, "enable_all_ads = $isEnableAllAds")
        } catch (e: Exception) {
            Log.e(TAG, "Parse enable_all_ads error: ${e.message}")
        }
    }

    private fun parseAdConfig(sp: SpManager) {
        try {
            val json = remoteConfig?.getString(KEY_ADS_CONFIG) ?: ""
            if (json.isNotBlank()) {
                val oldJson = sp.getString(KEY_ADS_CONFIG, "") ?: ""
                adConfig = gson.fromJson(json, AdConfig::class.java)
                if (json != oldJson) {
                    sp.putString(KEY_ADS_CONFIG, json)
                    Log.d(TAG, "Updated ad_config from remote")
                } else {
                    Log.d(TAG, "ad_config unchanged")
                }
            } else {
                Log.d(TAG, "ad_config is empty from remote, using local/default")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse ad_config error: ${e.message}")
        }
    }

    private fun parseEnableForceUpdate(sp: SpManager) {
        try {
            val remoteValue = remoteConfig?.getBoolean(KEY_ENABLE_FORCE_UPDATE) ?: false
            enableForceUpdate = remoteValue
            sp.putBoolean(KEY_ENABLE_FORCE_UPDATE, remoteValue)
            Log.d(TAG, "enable_force_update = $enableForceUpdate")
        } catch (e: Exception) {
            Log.e(TAG, "Parse enable_force_update error: ${e.message}")
        }
    }

    private fun parseNewVersionName(sp: SpManager) {
        try {
            val remoteValue = remoteConfig?.getString(KEY_NEW_VERSION_NAME)?.trim() ?: ""
            newVersionName = remoteValue
            sp.putString(KEY_NEW_VERSION_NAME, remoteValue)
            Log.d(TAG, "new_version_name = '$newVersionName'")
        } catch (e: Exception) {
            Log.e(TAG, "Parse new_version_name error: ${e.message}")
        }
    }

    // ========================= PUBLIC HELPERS =========================

    fun getString(key: String, defaultValue: String = ""): String =
        remoteConfig?.getString(key)?.ifEmpty { defaultValue } ?: defaultValue

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        runCatching { remoteConfig?.getBoolean(key) }.getOrElse { defaultValue } ?: defaultValue

    fun getLong(key: String, defaultValue: Long = 0L): Long =
        runCatching { remoteConfig?.getLong(key) }.getOrElse { defaultValue } ?: defaultValue
}
