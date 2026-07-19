package com.example.myapplication.ui.main

import com.example.myapplication.R
import com.example.myapplication.ads.AdMobAds
import com.example.myapplication.ads.AdUnits
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.alertfull.NotificationFSUtil
import com.example.myapplication.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.example.myapplication.ui.alertfull.PermissionFragment
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import com.libads.core.AdManager
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdShowCallback

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun initView() {
        NotificationFSUtil.createNotificationChannel(this)
        NotificationUtils.cancelOnboardingReminder(this)
        spManager.isCompletedOnboarding = true
        showBanner()
        binding.tvShowInter.setOnClickListener {
            showInterstitial()
        }

        binding.tvShowReward.setOnClickListener {
            showRewarded()
        }

        binding.tvShowInterNative.setOnClickListener {
            showNative()
        }
    }

    override fun initData() {
        AdManager.getInstance().preload(AdUnits.mainInterstitial)
        AdManager.getInstance().preload(AdUnits.mainRewarded)
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
        AdManager.getInstance().show(this, AdUnits.mainInterstitial, object : AdShowCallback {
            override fun onAdDismissed() {
                showToast("Interstitial dismissed")
                AdManager.getInstance().preload(AdUnits.mainInterstitial)
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                showToast("Interstitial failed: $message")
                AdManager.getInstance().preload(AdUnits.mainInterstitial)
            }
        })
    }

    private fun showRewarded() {
        AdManager.getInstance().show(this, AdUnits.mainRewarded, object : AdShowCallback {
            override fun onUserEarnedReward(amount: Int, type: String) {
                showToast("Reward earned: $amount $type")
            }

            override fun onAdDismissed() {
                showToast("Rewarded dismissed")
                AdManager.getInstance().preload(AdUnits.mainRewarded)
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                showToast("Rewarded failed: $message")
                AdManager.getInstance().preload(AdUnits.mainRewarded)
            }
        })
    }

    private fun showBanner() {
        AdMobAds.showBanner(binding.frBanner, CollapsiblePositionType.TOP)
    }

    private fun showNative() {
        AdMobAds.showNative(binding.frNative)
    }

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "PermissionFragment"
    }
}
