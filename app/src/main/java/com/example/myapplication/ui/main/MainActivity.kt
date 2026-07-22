package com.example.myapplication.ui.main

import android.os.CountDownTimer
import android.view.View
import com.example.myapplication.R
import com.example.myapplication.ads.AdMobAds
import com.example.myapplication.ads.AdUnits
import com.example.myapplication.ads.FullScreenAdUtils
import com.example.myapplication.ads.NativeAdUtils
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.alertfull.NotificationFSUtil
import com.example.myapplication.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.example.myapplication.ui.alertfull.PermissionFragment
import com.example.myapplication.ui.dialog.DialogLoadingAds
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import com.libads.core.AdManager
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback

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
        AdMobAds.preloadAppOpenResume()
        AdManager.getInstance().preload(AdUnits.mainInterstitial)
        AdManager.getInstance().preload(AdUnits.mainRewarded)
        AdManager.getInstance().preload(AdUnits.mainRewardedInterstitial)
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
        FullScreenAdUtils.showInterstitial(
            activity = this,
            onDismissed = { showToast("Interstitial dismissed") },
            onFailed = { message -> showToast("Interstitial failed: $message") }
        )
    }

    private fun showRewarded() {
        FullScreenAdUtils.showRewarded(
            activity = this,
            onRewardEarned = { amount, type -> showToast("Reward earned: $amount $type") },
            onDismissed = { showToast("Rewarded dismissed") },
            onFailed = { message -> showToast("Rewarded failed: $message") }
        )
    }

    private fun loadAndShowInterstitial() {
        loadAndShowWithLoading(
            adUnit = AdUnits.mainInterstitial,
            adName = "LoadAndShow interstitial",
            rewardPrefix = null
        )
    }

    private fun loadAndShowRewarded() {
        loadAndShowWithLoading(
            adUnit = AdUnits.mainRewarded,
            adName = "LoadAndShow rewarded",
            rewardPrefix = "Reward earned"
        )
    }

    private fun loadAndShowRewardedInterstitial() {
        loadAndShowWithLoading(
            adUnit = AdUnits.mainRewardedInterstitial,
            adName = "Rewarded interstitial",
            rewardPrefix = "Rewarded interstitial earned"
        )
    }

    private fun loadAndShowWithLoading(
        adUnit: com.libads.core.AdUnit,
        adName: String,
        rewardPrefix: String?
    ) {
        val loadingDialog = DialogLoadingAds(this)
        loadingDialog.show()
        AdManager.getInstance().loadAndShow(
            activity = this,
            adUnit = adUnit,
            callback = createLoadAndShowCallback(
                adName = adName,
                rewardPrefix = rewardPrefix,
                loadingDialog = loadingDialog
            )
        )
    }

    private fun createLoadAndShowCallback(
        adName: String,
        rewardPrefix: String?,
        loadingDialog: DialogLoadingAds
    ): AdShowCallback {
        return object : AdShowCallback {
            override fun onAdShown() {
                loadingDialog.dismiss()
            }

            override fun onUserEarnedReward(amount: Int, type: String) {
                rewardPrefix?.let { showToast("$it: $amount $type") }
            }

            override fun onAdDismissed() {
                loadingDialog.dismiss()
                showToast("$adName dismissed")
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                loadingDialog.dismiss()
                showToast("$adName failed: $message")
            }
        }
    }

    private fun showBanner() {
        binding.frBannerAds.shimmerBanner.visibility = View.VISIBLE
        binding.frBannerAds.shimmerBanner.startShimmer()
        binding.frBannerAds.bannerContainer.post {
            AdMobAds.showBanner(binding.frBannerAds.bannerContainer, CollapsiblePositionType.BOTTOM) { result ->
                binding.frBannerAds.shimmerBanner.stopShimmer()
                binding.frBannerAds.shimmerBanner.visibility = View.GONE
                if (result is AdResult.Failure) {
                    showToast("Banner failed: ${result.message}")
                }
            }
        }
    }

    private fun showNative() {
        NativeAdUtils.showNative(
            context = this,
            nativeContainer = binding.frNative,
            onFailure = { message -> showToast("Native failed: $message") }
        )
    }

    private fun showInterstitialThenNativeTimeout() {
        FullScreenAdUtils.showInterstitial(
            activity = this,
            onDismissed = { showNativeTimeout() },
            onFailed = { showNativeTimeout() }
        )
    }

    private fun showNativeTimeout() {
        nativeCloseTimer?.cancel()
        nativeCloseTimer = NativeAdUtils.showNativeWithCountdown(
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
