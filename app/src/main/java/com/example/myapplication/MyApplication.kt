package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.applovin.sdk.AppLovinPrivacySettings
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.bytedance.sdk.openadsdk.api.PAGConstant
import com.example.myapplication.base_ads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.base_ads.consent.UMPUtils.Companion.md5
import com.example.myapplication.base_ads.utils.AdPlacement
import com.example.myapplication.base_ads.utils.AdsEx
import com.example.myapplication.base_ads.utils.AppOpenAdsUtil
import com.example.myapplication.utils.AppEx.setupAppShortcuts
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.FirebaseConfigManager
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.notification.NotificationUtils
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.ads.mediation.inmobi.InMobiConsent
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.ResponseInfo
import com.google.android.ump.FormError
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.inmobi.sdk.InMobiSdk
import com.ironsource.mediationsdk.IronSource
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.tiktok.TikTokBusinessSdk
import com.tiktok.appevents.base.EventName
import com.tiktok.appevents.base.TTBaseEvent
import com.tiktok.appevents.contents.TTContentsEventConstants
import com.tiktok.appevents.contents.TTPurchaseEvent
import com.vungle.ads.VunglePrivacySettings
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale
import javax.inject.Singleton
import kotlin.collections.get

@HiltAndroidApp
@Singleton
class MyApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private var startedActivityCount = 0
    private var isSDKInitialized = false
    val spManager: SpManager
        get() = SpManager.get(applicationContext)
    private var currentActivity: Activity? = null
    private lateinit var openResumeAds: AppOpenAdsUtil

    companion object {
        private const val TAG = "TAG_APP"
        private const val DEFAULT_CURRENCY = "USD"
        private const val MICROS_PER_UNIT = 1_000_000.0

        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        @SuppressLint("StaticFieldLeak")
        private var mInstance: MyApplication? = null
        val instance get() = mInstance

        var isInterstitialShowing = false
        var isRewardedShowing = false
        var isAppOpenShowing = false

        fun isAnyAdShowing(): Boolean =
            isInterstitialShowing || isRewardedShowing || isAppOpenShowing
    }

    override fun onCreate() {
        super<Application>.onCreate()
        mInstance = this
        context = applicationContext
        setupAppShortcuts(this)
        registerActivityLifecycleCallbacks(this)

        FirebaseApp.initializeApp(this)
        FirebaseConfigManager.instance().fetch()

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        initTiktokSDK()
        initAppsflyer()
        initAdjust()
        initMediation()
    }
    @SuppressLint("HardwareIds")
    fun initConsentManager(
        activity: Activity,
        testDeviceIds: List<String> = emptyList(),
        onConsentComplete: () -> Unit
    ) {
        val consentManager = GoogleMobileAdsConsentManager.getInstance(this)
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val hashedId = md5(androidId).uppercase(Locale.getDefault())
        val ids = testDeviceIds.ifEmpty { listOf(hashedId) }

        Log.i(TAG, "Consent hashedId: $hashedId")
        consentManager.gatherConsent(
            activity,
            ids,
            object : GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener {
                override fun onConsentGatheringComplete(formError: FormError?) {
                    val granted = consentManager.canRequestAds()
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    } else {
                        Log.d(TAG, "Consent complete. Can request ads: $granted")
                    }
                    spManager.putBoolean(Constant.KEY_SP_UMP_SHOWED, granted)
                    onConsentComplete()
                }
            }
        )
    }
    fun initSDKs() {
        if (isSDKInitialized) return
        isSDKInitialized = true

        MobileAds.initialize(this) { status ->
            Log.d(TAG, "MobileAds initialized: ${status.adapterStatusMap.keys.joinToString()}")
        }

        FacebookSdk.setAutoLogAppEventsEnabled(true)

        val requestConfiguration = RequestConfiguration.Builder().build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        val sdk = MBridgeSDKFactory.getMBridgeSDK()
        sdk.setConsentStatus(this, MBridgeConstans.IS_SWITCH_ON)
        sdk.setDoNotTrackStatus(this, false)
        PangleMediationAdapter.setPAConsent(PAGConstant.PAGPAConsentType.PAG_PA_CONSENT_TYPE_CONSENT)
    }

    private fun initMediation() {
        initPangle()
        initVungle()
        initApplovin()
        initFAN()
        initMintegral()
        initInMobi()
        initIronSource()
    }

    private fun initFAN() {
        com.facebook.ads.AdSettings.setDataProcessingOptions(arrayOf())
        com.facebook.ads.AudienceNetworkAds.buildInitSettings(this)
            .withInitListener { result ->
                if (result.isSuccess) {
                    Log.d("FAN", "AudienceNetwork initialized successfully")
                } else {
                    Log.e("FAN", "AudienceNetwork init failed: ${result.message}")
                }
            }
            .initialize()
    }

    private fun initVungle() {
        VunglePrivacySettings.setGDPRStatus(true, "v1.0.0");
    }

    private fun initPangle() {
        // no request code
    }

    private fun initApplovin() {
        AppLovinPrivacySettings.setDoNotSell(false, this)
        VunglePrivacySettings.setCCPAStatus(true)
    }

    private fun initInMobi() {
        val consentObject = JSONObject()
        try {
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true)
            consentObject.put("gdpr", "1")
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }

        InMobiConsent.updateGDPRConsent(consentObject)
    }

    private fun initMintegral() {
        val sdk = MBridgeSDKFactory.getMBridgeSDK()
        sdk.setConsentStatus(this, MBridgeConstans.IS_SWITCH_ON)
    }

    private fun initIronSource() {
        IronSource.setConsent(true)
        IronSource.setMetaData("do_not_sell", "true")
    }

    fun loadAdsOpenResume(activity: Activity) {
        Log.i(TAG, "loadAdsOpenResume: $activity")
        openResumeAds = AppOpenAdsUtil(
            idAds = AdsEx.getAppOpenId(FirebaseConfigManager.instance().adConfig.appopen_resume.id),
            idAds2 = AdsEx.getAppOpenId(FirebaseConfigManager.instance().adConfig.appopen_resume.id_2f),
            adPlacement = AdPlacement.APPOPEN_RESUME,
            isEnable = FirebaseConfigManager.instance().adConfig.appopen_resume.enabled,
            checkOtherAdsShowing = { isAnyAdShowing() }
        )
        openResumeAds.load(activity)
    }

    private fun initTiktokSDK() {

        val tiktokAppId = getString(R.string.tiktok_app_id)

        val ttConfig = if (BuildConfig.DEBUG) {
            TikTokBusinessSdk.TTConfig(applicationContext)
                .setAppId(applicationContext.packageName)
                .setTTAppId(tiktokAppId).openDebugMode()
                .setLogLevel(TikTokBusinessSdk.LogLevel.DEBUG)
                .openDebugMode()
                .enableAutoIapTrack()
        } else {
            TikTokBusinessSdk.TTConfig(applicationContext)
                .setAppId(applicationContext.packageName)
                .setTTAppId(tiktokAppId)
                .enableAutoIapTrack()
        }
        TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
            override fun success() {}
            override fun fail(code: Int, msg: String?) {}
        })

        TikTokBusinessSdk.startTrack()
    }

    private fun reportRevForTiktok(
        currencyCode: String,
        mValueMicros: String,
        id: String,
        adType: String
    ) {
        try {
            if (currencyCode.isEmpty())
                return

            if (mValueMicros.isEmpty())
                return

            if (adType.isEmpty())
                return

            val tiktokRevenueRateForAdsImpressionEvent = 1000000

            val valueMicros = BigDecimal(mValueMicros)

            val valueAdImpression =
                valueMicros.divide(
                    BigDecimal(tiktokRevenueRateForAdsImpressionEvent),
                    3,
                    RoundingMode.HALF_UP
                )

            val adInfo = TTBaseEvent.newBuilder(EventName.IN_APP_AD_IMPR.toString())
                .addProperty("currency", currencyCode)
                .addProperty("value", valueAdImpression)
                .addProperty(
                    "Tiktok_RevenueRateForAdsImpressionEvent",
                    tiktokRevenueRateForAdsImpressionEvent
                )
                .addProperty("content_id", id)
                .addProperty("content_type", adType)
                .build()
            TikTokBusinessSdk.trackTTEvent(adInfo)

            val purchaseInfo = TTPurchaseEvent.newBuilder("Purchase")
                .setContentType(adType)
                .setContentId(id)
                .setCurrency(TTContentsEventConstants.Currency.USD)
                .setValue(valueAdImpression.toDouble())
                .addProperty(
                    "Tiktok_RevenueRateForPurchaseEvent",
                    tiktokRevenueRateForAdsImpressionEvent
                )
                .build()

            TikTokBusinessSdk.trackTTEvent(purchaseInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAppsflyer() {
        val appsflyerDevKey = getString(R.string.appsflyer_key)
        AppsFlyerLib.getInstance().init(appsflyerDevKey, null, this)
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG)
        AppsFlyerLib.getInstance().start(this, appsflyerDevKey, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(TAG, "Launch sent successfully, got 200 response code from server");
            }

            override fun onError(i: Int, s: String) {
                Log.d(
                    TAG, "Launch failed to be sent:\n" +
                            "Error code: " + i + "\n"
                            + "Error description: " + s
                );
            }
        })
    }

    private fun initAdjust() {
        val adjustAppToken = getString(R.string.adjust_app_token)
        val environment = if (BuildConfig.DEBUG)
            AdjustConfig.ENVIRONMENT_SANDBOX
        else
            AdjustConfig.ENVIRONMENT_PRODUCTION

        val config = AdjustConfig(this, adjustAppToken, environment)
        config.setLogLevel(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.WARN)
        Adjust.initSdk(config)
        Log.d(TAG, "Adjust SDK initialized (env=$environment)")
    }

    private fun logRevForAppsflyer(
        valueMicroStr: String?,
        networkAdapter: String?,
        currencyCode: String?,
        adType: String?
    ) {
        runCatching {
            val valueMicro = (valueMicroStr?.toDoubleOrNull() ?: 0.0) / 1_000_000

            if (valueMicro == 0.0)
                return

            val afAdRevData = AFAdRevenueData(
                monetizationNetwork = networkAdapter ?: "",
                mediationNetwork = MediationNetwork.GOOGLE_ADMOB,
                currencyIso4217Code = currencyCode ?: "USD",
                revenue = valueMicro
            )
            AppsFlyerLib.getInstance().logAdRevenue(
                afAdRevData, mapOf(
                    AdRevenueScheme.AD_TYPE to adType,
                )
            )
        }
    }

    private fun logRevForAdjust(
        adValue: AdValue,
        adUnitId: String,
        networkAdapter: String,
        adType: String
    ) {
        runCatching {
            val revenue = adValue.valueMicros / MICROS_PER_UNIT
            if (revenue <= 0) return

            val adRevenue = AdjustAdRevenue("admob_sdk")
            adRevenue.setRevenue(revenue, DEFAULT_CURRENCY)
            adRevenue.adRevenueNetwork = networkAdapter
            adRevenue.adRevenuePlacement = adUnitId
            adRevenue.adRevenueUnit = adType
            Adjust.trackAdRevenue(adRevenue)

            Log.d(TAG, "Adjust ad revenue tracked: $revenue USD ($adType / $adUnitId)")
        }
    }

    private fun logRevForFirebase(
        adValue: AdValue,
        adUnitId: String,
        networkAdapter: String,
        adType: String
    ) {
        runCatching {
            val revenue = adValue.valueMicros / MICROS_PER_UNIT
            if (revenue <= 0) return

            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                putString(FirebaseAnalytics.Param.AD_SOURCE, networkAdapter)
                putString(FirebaseAnalytics.Param.AD_FORMAT, adType)
                putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnitId)
                putString(FirebaseAnalytics.Param.CURRENCY, DEFAULT_CURRENCY)
                putDouble(FirebaseAnalytics.Param.VALUE, revenue)
            }
            FirebaseAnalytics.getInstance(this)
                .logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle)

            Log.d(TAG, "Firebase ad_impression logged: $revenue USD ($adType / $adUnitId)")
        }
    }

    fun handleAdRevenue(
        adValue: AdValue,
        adUnitId: String = "",
        responseInfo: ResponseInfo? = null,
        adType: String = "unknown"
    ) {
        val revenue = adValue.valueMicros / MICROS_PER_UNIT

        if (revenue <= 0) return

        val networkAdapter = responseInfo?.mediationAdapterClassName ?: "admob"
        val placementId = adUnitId.ifEmpty { "unknown_${adType}_${System.currentTimeMillis()}" }

        Log.d(TAG, "handleAdRevenue: $revenue USD | type=$adType | placement=$placementId | network=$networkAdapter")

        // 1. Facebook
        pushRevAdmobForFacebook(adValue.valueMicros.toDouble())

        // 2. AppsFlyer
        logRevForAppsflyer(
            valueMicroStr = adValue.valueMicros.toString(),
            networkAdapter = networkAdapter,
            currencyCode = DEFAULT_CURRENCY,
            adType = adType
        )

        // 3. TikTok
        reportRevForTiktok(
            currencyCode = DEFAULT_CURRENCY,
            mValueMicros = adValue.valueMicros.toString(),
            id = placementId,
            adType = adType
        )

        // 4. Adjust
        logRevForAdjust(
            adValue = adValue,
            adUnitId = placementId,
            networkAdapter = networkAdapter,
            adType = adType
        )

        // 5. Firebase Analytics (GA4 ad_impression)
        logRevForFirebase(
            adValue = adValue,
            adUnitId = placementId,
            networkAdapter = networkAdapter,
            adType = adType
        )
    }

    private fun pushRevAdmobForFacebook(valueMicros: Double) {
        val value = valueMicros / 1_000_000.0

        // Log purchase event
        AppEventsLogger
            .newLogger(this)
            .logPurchase(BigDecimal.valueOf(value), Currency.getInstance("USD"))

        // Log ad impression event
        val bundle = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")
        }

        AppEventsLogger
            .newLogger(this)
            .logEvent(AppEventsConstants.EVENT_NAME_AD_IMPRESSION, value, bundle)
    }
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i("TAG_APP", "onStart: $currentActivity")
        if (::openResumeAds.isInitialized && !isAnyAdShowing()) {
            currentActivity?.let { openResumeAds.showIfAvailable(it) }
        }
    }
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
        startedActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.isChangingConfigurations) return

        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
        if (startedActivityCount == 0) {
            val sp = SpManager.get(applicationContext)
            if (!sp.isCompletedOnboarding) {
                NotificationUtils.scheduleOnboardingReminder(applicationContext)
            }
        }
    }
}
