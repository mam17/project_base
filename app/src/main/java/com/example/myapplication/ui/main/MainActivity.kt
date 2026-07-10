package com.example.myapplication.ui.main

import android.util.Log
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.base_ads.interfaces.OnAdmobShowListener
import com.example.myapplication.base_ads.utils.AdPlacement
import com.example.myapplication.base_ads.utils.AdsEx
import com.example.myapplication.base_ads.utils.InterstitialAdsUtil
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.alertfull.NotificationFSUtil
import com.example.myapplication.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.example.myapplication.ui.alertfull.PermissionFragment
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val interHome by lazy {
        InterstitialAdsUtil(
            context = this,
            idAds = AdsEx.getInterstitialId(remoteConfig.inter_home.id),
            idAds2f = remoteConfig.inter_home.id_2f.takeIf { it.isNotEmpty() }
                ?.let { AdsEx.getInterstitialId(it) },
            adPlacement = AdPlacement.INTER_HOME,
            isEnable = remoteConfig.inter_home.enabled
        )
    }


    override fun initView() {
        NotificationFSUtil.createNotificationChannel(this)
        NotificationUtils.cancelOnboardingReminder(this)
        spManager.isCompletedOnboarding = true

        binding.root.setOnClickListener {
            startNextActivity(LanguageActivity::class.java)
        }
    }

    override fun initData() {
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

    fun showInterHome(action: () -> Unit) {
        interHome.show(this, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d("TAG_INTER_HOME", "Home inter showed")
            }

            override fun onError(e: String) {
                Log.d("TAG_INTER_HOME", "Show error: $e")
                action.invoke()
            }

            override fun onClosed() {
                super.onClosed()
                Log.d("TAG_INTER_HOME", "Home inter closed")
                action.invoke()
            }
        }, true)
    }

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "PermissionFragment"
    }
}