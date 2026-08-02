package com.example.myapplication.ui.main

import android.os.CountDownTimer
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.ads.AdBannerUtils
import com.example.myapplication.ads.AdInterstitialUtils
import com.example.myapplication.ads.AdNativeUtils
import com.example.myapplication.ads.AdRewardUtils
import com.example.myapplication.ads.AdsPreloadCoordinator
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.alertfull.NotificationFSUtil
import com.example.myapplication.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.example.myapplication.ui.alertfull.PermissionFragment
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdResult

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private var nativeCloseTimer: CountDownTimer? = null

    override fun initView() {
        setBaseFullScreen()
        NotificationFSUtil.createNotificationChannel(this)
        NotificationUtils.cancelOnboardingReminder(this)
        spManager.isCompletedOnboarding = true
        binding.frNativeTimeOut.root.visibility = View.GONE
        binding.frNativeTimeOut.nativeFullContainer.visibility = View.GONE
        binding.frNativeTimeOut.nativeFullLoadingContainer.visibility = View.GONE
        binding.frNativeTimeOut.rlCloseAds.visibility = View.GONE
        binding.frNativeTimeOut.tvTimeCount.visibility = View.VISIBLE
        binding.frNativeTimeOut.btnCloseOnb.visibility = View.GONE
        AdsPreloadCoordinator.start()
        showBanner()
        binding.tvShowInter.setOnClickListener {
            showInterstitial()
        }

        binding.tvShowReward.setOnClickListener {
            showRewarded()
        }

        binding.tvShowInterNative.setOnClickListener {
            showInterstitialThenNativeTimeout()
        }
        binding.tvLoadAndShowInter.setOnClickListener {
            loadAndShowInterstitial()
        }
        binding.tvLoadAndShowReward.setOnClickListener {
            loadAndShowRewarded()
        }
        binding.tvShowRewardInter.setOnClickListener {
            loadAndShowRewardedInterstitial()
        }
    }

    override fun initData() {
        showNative()
    }


    override fun onBack() {
        showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_exit_app),
            strCancel = getString(R.string.txt_confirm),
            strYes = getString(R.string.txt_cancel),
            okOnClick = {},
            cancelOnClick = {
                super.onBack()
            })
    }

    override fun onResume() {
        super.onResume()
        ensureFeaturePermissions()
    }

    fun ensureFeaturePermissions(): Boolean {
        if (!PermissionUtils.hasAllPermissions(this)) {
            val fm = supportFragmentManager
            val existing = fm.findFragmentByTag(PERMISSION_FRAGMENT_TAG) as? PermissionFragment

            if (existing == null || !existing.isAdded) {
                val fragment = PermissionFragment()
                fragment.show(fm, PERMISSION_FRAGMENT_TAG)
            }
            return false
        } else {
            (supportFragmentManager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) as? PermissionFragment)
                ?.dismissAllowingStateLoss()

            scheduleFullScreenNotificationDiary(this)
            return true
        }
    }

    private fun showInterstitial() {
        AdInterstitialUtils.show(
            activity = this,
            onDismissed = { showToast("Interstitial dismissed") },
            onFailed = { message -> showToast("Interstitial failed: $message") }
        )
    }

    private fun showRewarded() {
        AdRewardUtils.showRewarded(
            activity = this,
            onRewardEarned = { amount, type -> showToast("Reward earned: $amount $type") },
            onDismissed = { showToast("Rewarded dismissed") },
            onFailed = { message -> showToast("Rewarded failed: $message") }
        )
    }

    private fun loadAndShowInterstitial() {
        AdInterstitialUtils.loadAndShow(
            activity = this,
            onDismissed = { showToast("LoadAndShow interstitial dismissed") },
            onFailed = { message -> showToast("LoadAndShow interstitial failed: $message") }
        )
    }

    private fun loadAndShowRewarded() {
        AdRewardUtils.loadAndShowRewarded(
            activity = this,
            onRewardEarned = { amount, type -> showToast("Reward earned: $amount $type") },
            onDismissed = { showToast("LoadAndShow rewarded dismissed") },
            onFailed = { message -> showToast("LoadAndShow rewarded failed: $message") }
        )
    }

    private fun loadAndShowRewardedInterstitial() {
        AdRewardUtils.loadAndShowRewardedInterstitial(
            activity = this,
            onRewardEarned = { amount, type ->
                showToast("Rewarded interstitial earned: $amount $type")
            },
            onDismissed = { showToast("Rewarded interstitial dismissed") },
            onFailed = { message -> showToast("Rewarded interstitial failed: $message") }
        )
    }

    private fun showBanner() {
        binding.frBannerAds.shimmerBanner.visibility = View.VISIBLE
        binding.frBannerAds.shimmerBanner.startShimmer()
        binding.frBannerAds.bannerContainer.post {
            AdBannerUtils.showBanner(
                binding.frBannerAds.bannerContainer,
                CollapsiblePositionType.BOTTOM
            ) { result ->
                binding.frBannerAds.shimmerBanner.stopShimmer()
                binding.frBannerAds.shimmerBanner.visibility = View.GONE
                if (result is AdResult.Failure) {
                    showToast("Banner failed: ${result.message}")
                }
            }
        }
    }

    private fun showNative() {
        AdNativeUtils.showNative(
            context = this,
            nativeContainer = binding.frNative,
            onFailure = { message -> showToast("Native failed: $message") }
        )
    }

    private fun showInterstitialThenNativeTimeout() {
        AdInterstitialUtils.show(
            activity = this,
            onDismissed = { showNativeTimeout() },
            onFailed = { showNativeTimeout() }
        )
    }

    private fun showNativeTimeout() {
        nativeCloseTimer?.cancel()
        nativeCloseTimer = AdNativeUtils.showNativeWithCountdown(
            rootView = binding.frNativeTimeOut.root,
            nativeContainer = binding.frNativeTimeOut.nativeFullContainer,
            loadingContainer = binding.frNativeTimeOut.nativeFullLoadingContainer,
            closeContainer = binding.frNativeTimeOut.rlCloseAds,
            timeCountView = binding.frNativeTimeOut.tvTimeCount,
            closeButton = binding.frNativeTimeOut.btnCloseOnb,
            countdownSeconds = NATIVE_CLOSE_COUNTDOWN_SECONDS,
            onClose = {
                nativeCloseTimer?.cancel()
                nativeCloseTimer = null
                startNextActivity(LanguageActivity::class.java)
            },
            onFailure = { message -> showToast("Native failed: $message") }
        )
    }

    override fun onDestroy() {
        nativeCloseTimer?.cancel()
        nativeCloseTimer = null
        super.onDestroy()
    }

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "PermissionFragment"
        private const val NATIVE_CLOSE_COUNTDOWN_SECONDS = 3L
    }
}
