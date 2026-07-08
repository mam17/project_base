package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.BuildConfig
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.base_ads.admods.InterstitialAds
import com.example.myapplication.base_ads.admods.NativeAds
import com.example.myapplication.base_ads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.base_ads.interfaces.OnAdmobLoadListener
import com.example.myapplication.base_ads.interfaces.OnAdmobShowListener
import com.example.myapplication.base_ads.utils.AdPlacement
import com.example.myapplication.base_ads.utils.AdsEx
import com.example.myapplication.base_ads.utils.BannerAdsUntil
import com.example.myapplication.base_ads.utils.NativeAdsUtil
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.uninstall.UninstallActivity
import com.example.myapplication.utils.AppEx.openAppInStore
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.FirebaseConfigManager
import com.example.myapplication.utils.FirebaseTrackingManager
import com.example.myapplication.utils.NetworkUtil
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import com.example.myapplication.utils.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Notification check finished -> Go to Step 2: Version Update check
            checkVersionUpdate()
        }
        
    private var isCheckUninstall = false
    private var isStartingNextScreen = false
    private var isNetworkDialogShowing = false
    private var splashStartTime: Long = 0L
    private var isNextScreenOpened = false
    private var adsInterSplash: InterstitialAds? = null

    override fun initView() {
        splashStartTime = System.currentTimeMillis()
        isCheckUninstall = intent.getBooleanExtra(Constant.KEY_OPEN_SPLASH, false)
        trackNotificationOpen(intent)
        
        // Setup click listener for native ad close button
        binding.layoutNativeFSTimer.btnCloseOnb.setOnClickListener {
            openNextScreen()
        }
        
        // Step 1: Check Notification Permission
        checkNotificationPermissionFlow()
    }

    override fun initData() {
        // Nothing here
    }

    // ========================= STEP 1: NOTIFICATION PERMISSION =========================
    
    private fun checkNotificationPermissionFlow() {
        if (PermissionUtils.isNotificationPermissionGranted(this)) {
            checkVersionUpdate()
        } else {
            PermissionUtils.requestNotificationPermission(this, notificationPermissionLauncher)
        }
    }

    // ========================= STEP 2: VERSION UPDATE =========================
    
    private fun checkVersionUpdate() {
        checkNetworkAndFetchConfig()
    }

    // ========================= STEP 3: NETWORK CHECK & CONFIG FETCH =========================
    
    private fun checkNetworkAndFetchConfig() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showNetworkErrorDialog()
            return
        }

        // Fetch Remote Config
        FirebaseConfigManager.instance().fetch { success ->
            Log.i(TAG, "Remote config fetch finished. Success=$success")
            runOnUiThread {
                performVersionCheck()
            }
        }
    }

    private fun performVersionCheck() {
        val remoteConfig = FirebaseConfigManager.instance()

        if (remoteConfig.enableForceUpdate) {
            val currentAppVersion = BuildConfig.VERSION_NAME
            val remoteVersionName = remoteConfig.newVersionName

            Log.i(TAG, "performVersionCheck: current=$currentAppVersion, remote=$remoteVersionName")

            if (remoteVersionName.isNotEmpty() && currentAppVersion != remoteVersionName) {
                // Show force update dialog (block next steps)
                showDialogAlert(
                    strTitle = getString(R.string.txt_title_update),
                    strBody = getString(R.string.txt_cont_update),
                    strCancel = null,
                    strYes = getString(R.string.txt_update_now),
                    okOnClick = {
                        openAppInStore()
                    },
                    cancelOnClick = null
                )
            } else {
                // Version is up to date -> Go to Step 4: Consent Check
                checkConsentFlow()
            }
        } else {
            Log.i(TAG, "performVersionCheck: Force update disabled")
            // Go to Step 4: Consent Check
            checkConsentFlow()
        }
    }

    private fun showNetworkErrorDialog() {
        if (isNetworkDialogShowing) return
        isNetworkDialogShowing = true
        showDialogAlert(
            strTitle = getString(R.string.txt_network_error),
            strBody = getString(R.string.txt_network_error_message),
            strCancel = getString(R.string.txt_cancel),
            strYes = getString(R.string.txt_try_again),
            okOnClick = {
                isNetworkDialogShowing = false
                checkNetworkAndFetchConfig()
            },
            cancelOnClick = {
                isNetworkDialogShowing = false
                finish()
            }
        )
    }

    // ========================= STEP 4: CONSENT FLOW =========================
    
    private fun checkConsentFlow() {
        val consentManager = GoogleMobileAdsConsentManager.getInstance(this)
        
        MyApplication.instance?.initConsentManager(this) {
            runOnUiThread {
                // 1. Initialize all SDKs (AdMob, Facebook, TikTok, Adjust)
                (application as MyApplication).initSDKs()
                
                // 2. Check if we can request ads
                if (consentManager.canRequestAds()) {
                    startAdsFlow()
                } else {
                    Log.i(TAG, "Ads consent not granted -> Skip ads and proceed")
                    openNextScreen()
                }
            }
        } ?: run {
            openNextScreen()
        }
    }

    // ========================= ADS LOADING & SHOWING =========================
    
    private fun startAdsFlow() {
        // 1. Load Adaptive Banner
        initBannerAds()
        // 2. Load and Show Interstitial or Native Fullscreen Splash
        loadInterSplash()
    }

    private fun initBannerAds() {
        val remoteConfig = FirebaseConfigManager.instance().adConfig
        val bannerSplash = remoteConfig.banner_splash_first
        if (bannerSplash.enabled) {
            BannerAdsUntil.initBanner(
                activity = this,
                shimmer = binding.adViewContainer.shimmerBanner,
                primaryAdUnitId = AdsEx.getBannerId(bannerSplash.id),
                secondaryAdUnitId = AdsEx.getBannerId(bannerSplash.id_2f),
                adPlacement = AdPlacement.BANNER_SPLASH
            )
        } else {
            binding.adViewContainer.root.gone()
        }
    }

    private fun loadInterSplash() {
        val remoteConfig = FirebaseConfigManager.instance().adConfig
        val interSplash = remoteConfig.inter_splash_first
        if (interSplash.enabled) {
            val idUnit2f = AdsEx.getInterstitialId(interSplash.id_2f)
            val idUnit = AdsEx.getInterstitialId(interSplash.id)

            adsInterSplash = InterstitialAds(this, idUnit2f, AdPlacement.INTER_SPLASH)
            adsInterSplash?.load(object : OnAdmobLoadListener {
                override fun onLoad() {
                    showInterAdsSplash()
                }

                override fun onError(e: String) {
                    // Retry with standard ID
                    adsInterSplash = InterstitialAds(this@SplashActivity, idUnit, AdPlacement.INTER_SPLASH)
                    adsInterSplash?.load(object : OnAdmobLoadListener {
                        override fun onLoad() {
                            showInterAdsSplash()
                        }

                        override fun onError(e: String) {
                            showNativeSplash()
                        }
                    })
                }
            })
        } else {
            showNativeSplash()
        }
    }

    private fun showInterAdsSplash() {
        if (isFinishing || isDestroyed) return
        
        adsInterSplash?.show(this, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d(TAG, "onShow: Splash inter showed")
            }

            override fun onError(e: String) {
                Log.i(TAG, "onError showing Inter: $e -> Fallback to Native Splash")
                showNativeSplash()
            }

            override fun onClosed() {
                Log.i(TAG, "Splash inter closed -> Show Native Splash")
                showNativeSplash()
            }
        })
    }

    private fun showNativeSplash() {
        val remoteConfig = FirebaseConfigManager.instance().adConfig
        val idUnit = if (!isCheckOpenApp) remoteConfig.native_fs_splash_first else remoteConfig.native_fs_splash_second
        
        if (!idUnit.enabled) {
            Log.i(TAG, "showNativeSplash: Native Splash disabled")
            openNextScreen()
            return
        }

        val ad = NativeAdsUtil.splashNativeFullAdmob
        if (ad != null && ad.available()) {
            Log.i(TAG, "showNativeSplash: Ad available, showing")
            performShowNativeSplash(ad)
        } else {
            Log.i(TAG, "showNativeSplash: Ad not ready, loading...")
            NativeAdsUtil.loadNativeFullSplash(isFirstOpenApp = isCheckOpenApp) { loadedAd ->
                Log.i(TAG, "showNativeSplash: Ad loaded, showing")
                performShowNativeSplash(loadedAd)
            }

            // Fallback timeout to prevent getting stuck
            Handler(Looper.getMainLooper()).postDelayed({
                val currentAd = NativeAdsUtil.splashNativeFullAdmob
                if (currentAd == null || !currentAd.available()) {
                    Log.i(TAG, "showNativeSplash: load timeout -> proceed to next screen")
                    openNextScreen()
                }
            }, 8000)
        }
    }

    private fun performShowNativeSplash(ad: NativeAds) {
        runOnUiThread {
            if (isFinishing || isDestroyed) return@runOnUiThread
            
            if (ad.available()) {
                binding.layoutNativeFSTimer.root.visible()
                binding.layoutNativeFSTimer.shimmerNativeFullScreen.visible()

                ad.showNative(
                    binding.layoutNativeFSTimer.shimmerNativeFullScreen,
                    R.id.native_ad_view,
                    object : OnAdmobShowListener {
                        override fun onShow() {
                            Log.i(TAG, "performShowNativeSplash: Native Showed")
                            startNativeCountdown()
                        }

                        override fun onError(e: String) {
                            Log.e(TAG, "performShowNativeSplash error: $e")
                            openNextScreen()
                        }
                    }
                )
            } else {
                Log.i(TAG, "performShowNativeSplash: Ad not available in perform")
                openNextScreen()
            }
        }
    }

    private fun startNativeCountdown() {
        binding.layoutNativeFSTimer.rlCloseAds.visible()
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                if (seconds < 1) {
                    binding.layoutNativeFSTimer.tvTimeCount.gone()
                    binding.layoutNativeFSTimer.btnCloseOnb.visible()
                    cancel()
                } else {
                    binding.layoutNativeFSTimer.tvTimeCount.text = seconds.toString()
                }
            }

            override fun onFinish() {
                binding.layoutNativeFSTimer.tvTimeCount.gone()
                binding.layoutNativeFSTimer.btnCloseOnb.visible()
            }
        }.start()
    }

    // ========================= NAVIGATION =========================

    private fun openNextScreen() {
        if (isNextScreenOpened) return
        isNextScreenOpened = true
        
        val timeElapsed = System.currentTimeMillis() - splashStartTime
        val remainingDelay = SPLASH_MIN_DURATION_MS - timeElapsed
        
        lifecycleScope.launch {
            if (remainingDelay > 0) {
                delay(remainingDelay)
            }
            performOpenNextScreen()
        }
    }

    private fun performOpenNextScreen() {
        if (isCheckUninstall) {
            startNextActivity(UninstallActivity::class.java, isFinish = true)
        } else {
            if (spManager.isCompletedOnboarding) {
                startActivityNewTask(MainActivity::class.java)
            } else {
                val bundle = Bundle().apply { putBoolean(Constant.KEY_FROM_SPLASH, true) }
                startNextActivity(LanguageActivity::class.java, bundle, isFinish = true)
            }
        }
    }

    private fun trackNotificationOpen(intent: Intent?) {
        if (intent?.getBooleanExtra(
                NotificationUtils.EXTRA_OPEN_FROM_NOTIFICATION,
                false
            ) != true
        ) {
            return
        }

        Log.i(TAG, "trackNotificationOpen: EVENT_NOTIFICATION_CLICK_OPEN_APP")
        FirebaseTrackingManager.instance().logEvent(
            FirebaseTrackingManager.EVENT_NOTIFICATION_CLICK_OPEN_APP
        )
        intent.removeExtra(NotificationUtils.EXTRA_OPEN_FROM_NOTIFICATION)
    }

    override fun onDestroy() {
        adsInterSplash?.destroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TAG_SPLASH"
        private const val SPLASH_MIN_DURATION_MS = 1_500L
    }
}