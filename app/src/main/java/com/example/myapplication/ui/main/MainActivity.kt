package com.example.myapplication.ui.main

import android.os.CountDownTimer
import android.view.View
import com.example.ads.banner.presentation.enums.BannerAdKey
import com.example.ads.banner.presentation.viewModels.ViewModelBanner
import com.example.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.example.ads.interstitial.enums.InterAdKey
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.example.ads.rewarded.enums.RewardedAdKey
import com.example.ads.utilities.extensions.addCleanView
import com.example.ads.utilities.extensions.logButtonClick
import com.example.ads.utilities.extensions.logScreenView
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModelBanner by viewModel<ViewModelBanner>()
    private var homeNativeAd: NativeAd? = null
    private var countdownTimer: CountDownTimer? = null

    override fun initView() {
        setBaseFullScreen()
        binding.layoutFullScreenCount.root.gone()
        loadBanner()

        logScreenView("MainActivity")

        binding.root.setOnClickListener {
            startNextActivity(LanguageActivity::class.java)
        }

        binding.btnShowInter.setOnClickListener {
            logButtonClick("show_interstitial")

            val action = { startNextActivity(LanguageActivity::class.java) }
            diComponent.interstitialAdsConfig.loadInterstitialAd(
                activity = this,
                adType = InterAdKey.INTER_HOME,
                listener = object : InterstitialOnLoadCallBack {
                    override fun onResponse(successfullyLoaded: Boolean) {
                        if (successfullyLoaded) {
                            showInterAd(
                                InterAdKey.INTER_HOME,
                                onDismiss = { showNativeHomeOverlay(action) },
                                onFailed = { showNativeHomeOverlay(action) }
                            )
                        } else {
                            showNativeHomeOverlay(action)
                        }
                    }
                }
            )
        }

        binding.btnShowReward.setOnClickListener {
            logButtonClick("show_rewarded")

            diComponent.rewardedAdsConfig.loadRewardedAd(
                activity = this,
                adType = RewardedAdKey.AI_FEATURE,
                listener = object : RewardedOnLoadCallBack {
                    override fun onResponse(isSuccess: Boolean) {
                        if (isSuccess) {
                            showRewardedAd(
                                RewardedAdKey.AI_FEATURE,
                                onRewarded = { },
                                onDismiss = { startNextActivity(LanguageActivity::class.java) }
                            )
                        }
                    }
                }
            )
        }
    }

    override fun initData() {
        loadNativeAd(NativeAdKey.HOME, onLoaded = { homeNativeAd = it })
    }

    override fun initObserver() {
        super.initObserver()
        viewModelBanner.adViewLiveData.observe(this) {
            binding.bannerAdViewHome.addCleanView(it)
        }
        viewModelBanner.loadFailedLiveData.observe(this) {
            binding.bannerAdViewHome.visibility = View.GONE
        }
        viewModelBanner.clearViewLiveData.observe(this) {
            binding.bannerAdViewHome.removeAllViews()
        }
    }

    private fun showNativeHomeOverlay(action: () -> Unit) {
        with(binding.layoutFullScreenCount) {
            root.visible()
            homeNativeAd?.let { adNativeFullScreen.setNativeAd(it) }
            tvTimeCount.visibility = View.VISIBLE
            tvTimeCount.text = "3"
            btnCloseOnb.visibility = View.GONE
        }

        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(3000L, 1000L) {
            var count = 3

            override fun onTick(millisUntilFinished: Long) {
                binding.layoutFullScreenCount.tvTimeCount.text = count.toString()
                count--
            }

            override fun onFinish() {
                binding.layoutFullScreenCount.tvTimeCount.visibility = View.GONE
                binding.layoutFullScreenCount.btnCloseOnb.visibility = View.VISIBLE
                binding.layoutFullScreenCount.btnCloseOnb.setOnClickListener {
                    binding.layoutFullScreenCount.root.gone()
                    action()
                }
            }
        }.start()
    }

    private fun loadBanner() {
        val adView = AdView(this)
        viewModelBanner.loadBannerAd(adView, BannerAdKey.HOME)
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }

    override fun onBack() {
        showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_exit_app),
            strCancel = getString(R.string.txt_confirm),
            strYes = getString(R.string.txt_cancel),
            okOnClick = {},
            cancelOnClick = { super.onBack() }
        )
    }
}
