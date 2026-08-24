package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
 import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ads.AdNativeCountdown
import com.example.myapplication.ads.AdPlacement
import com.example.myapplication.ads.Ads
import com.example.myapplication.ads.AdsPreloadCoordinator
import com.example.myapplication.ads.AdUnits
import com.example.myapplication.ads.NativeType
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.uninstall.UninstallActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.firebase.FirebaseTrackingManager
import com.example.myapplication.utils.NetworkUtil
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import com.example.myapplication.utils.mmp.AppsFlyerMmpManager
import com.libads.core.consent.UmpConsentManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            goToNextAction()
        }
    private var isCheckUninstall = false
    private var isStartingNextScreen = false
    private var isNetworkDialogShowing = false
    private var nativeSplashTimer: CountDownTimer? = null

    // --- Ad placements for splash flow ---
    private val interSplash = AdPlacement.interstitial(AdUnits.INTER_SPLASH_FIRST)
    private val nativeSplashSecond = AdPlacement.native(AdUnits.NATIVE_FS_SPLASH_SECOND, NativeType.FULL_SCREEN)
    private val nativeSplashFirst = AdPlacement.native(AdUnits.NATIVE_FS_SPLASH_FIRST, NativeType.FULL_SCREEN)

    override fun initView() {
        AppsFlyerMmpManager.onLauncherActivityCreated(this)
        isCheckUninstall = intent.getBooleanExtra(Constant.KEY_OPEN_SPLASH, false)
        trackNotificationOpen(intent)

        if (PermissionUtils.isNotificationPermissionGranted(this)) {
            goToNextAction()
        } else {
            PermissionUtils.requestNotificationPermission(this, notificationPermissionLauncher)
        }
    }

    override fun initData() {

    }

    private fun trackNotificationOpen(intent: Intent?) {
        if (intent?.getBooleanExtra(
                NotificationUtils.EXTRA_OPEN_FROM_NOTIFICATION,
                false
            ) != true
        ) {
            return
        }

        Log.i("TAG_SPLASH", "trackNotificationOpen: EVENT_NOTIFICATION_CLICK_OPEN_APP")
        FirebaseTrackingManager.instance().logEvent(FirebaseTrackingManager.EVENT_NOTIFICATION_CLICK_OPEN_APP)
        intent.removeExtra(NotificationUtils.EXTRA_OPEN_FROM_NOTIFICATION)
    }

    fun goToNextAction() {
        if (isStartingNextScreen) return
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showNetworkErrorDialog()
            return
        }
        isStartingNextScreen = true
        val startedAt = System.currentTimeMillis()
        UmpConsentManager.gatherConsent(this) {
            AppsFlyerMmpManager.onConsentReady()
//            AdsPreloadCoordinator.start()

            // Preload splash ads early for best fill rate
            Ads.preload(interSplash)
            if (isCheckOpenApp){
                Ads.preload(nativeSplashFirst)
            }else{
                Ads.preload(nativeSplashSecond)
            }

            lifecycleScope.launch {
                val remainingDelay = SPLASH_MIN_DURATION_MS - (System.currentTimeMillis() - startedAt)
                if (remainingDelay > 0) delay(remainingDelay.milliseconds)
                showInterSplash()
            }
        }
    }

    /**
     * Step 1: Show interstitial splash.
     * On dismiss or fail → show native splash fullscreen.
     */
    private fun showInterSplash() {
        Ads.show(this, interSplash) {
            action { showNativeSplash() }
        }
    }

    /**
     * Step 2: Show native fullscreen splash with countdown.
     * On close (countdown finished + user tap) → navigate to next screen.
     */
    private fun showNativeSplash() {
        nativeSplashTimer?.cancel()
        nativeSplashTimer = AdNativeCountdown.show(
            lifecycleOwner = this,
            rootView = binding.frNativeTimeOut.root,
            nativeContainer = binding.frNativeTimeOut.nativeFullContainer,
            loadingContainer = binding.frNativeTimeOut.nativeFullLoadingContainer,
            closeContainer = binding.frNativeTimeOut.rlCloseAds,
            timeCountView = binding.frNativeTimeOut.tvTimeCount,
            closeButton = binding.frNativeTimeOut.btnCloseOnb,
            countdownSeconds = NATIVE_SPLASH_COUNTDOWN_SECONDS,
            onClose = {
                nativeSplashTimer?.cancel()
                nativeSplashTimer = null
                openNextScreen()
            },
            placement = nativeSplashFirst,
            onFailure = { message ->
                Log.w(TAG, "Native splash failed: $message, navigating directly")
                openNextScreen()
            }
        )

        // If timer is null, countdown couldn't start (lifecycle destroyed or no ad) → go next
        if (nativeSplashTimer == null) {
            openNextScreen()
        }
    }

    private fun openNextScreen() {
        if (isCheckUninstall) {
            startNextActivity(UninstallActivity::class.java, isFinish = true)
        } else {
//            if (spManager.isCompletedOnboarding) {
//                startActivityNewTask(MainActivity::class.java)
//            } else {
//                val bundle = Bundle().apply { putBoolean(Constant.KEY_FROM_SPLASH, true) }
//                startNextActivity(LanguageActivity::class.java, bundle, isFinish = true)
//            }
            val bundle = Bundle().apply { putBoolean(Constant.KEY_FROM_SPLASH, true) }
            startNextActivity(LanguageActivity::class.java, bundle, isFinish = true)
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
                goToNextAction()
            },
            cancelOnClick = {
                isNetworkDialogShowing = false
                finish()
            }
        )
    }

    override fun onDestroy() {
        nativeSplashTimer?.cancel()
        nativeSplashTimer = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TAG_SPLASH"
        private const val SPLASH_MIN_DURATION_MS = 1_500L
        private const val NATIVE_SPLASH_COUNTDOWN_SECONDS = 3L
    }
}
