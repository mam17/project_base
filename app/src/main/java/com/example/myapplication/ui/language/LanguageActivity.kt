package com.example.myapplication.ui.language

import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.base_ads.utils.AdsEx.observeOnce
import com.example.myapplication.base_ads.utils.NativeAdsUtil
import com.example.myapplication.databinding.ActivityLanguageBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.onboarding.OnboardingActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.invisible
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val viewModel: LanguageViewModel by viewModels()
    private var mLanguageAdapter = LanguageAdapter()
    private var fromSplash = false

    override fun initView() {
        fromSplash = intent.getBooleanExtra(Constant.KEY_FROM_SPLASH, false)
        Log.i("TAG_LANGUAGE", "initUI: fromSplash $fromSplash")
        if (fromSplash) {
            NativeAdsUtil.loadNativeOnb1(isCheckOpenApp)
            NativeAdsUtil.loadNativeOnb3(isCheckOpenApp)
        }
        initNativeAd1()
        initUI()
        initListener()
    }

    private fun initListener() {
        binding.apply {
            toolBarLanguage.btnSelect.setOnClickListener {
                mLanguageAdapter.getSelectedModel()?.let { model ->
                    SystemUtil.saveLanguage(this@LanguageActivity, model)
                    if (fromSplash) {
                        startActivityNewTask(MainActivity::class.java)
                    } else {
                        startNextActivity(OnboardingActivity::class.java, isFinish = true)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            toolBarLanguage.btnBack.isVisible = !fromSplash
            toolBarLanguage.btnSelect.isVisible = !fromSplash
            toolBarLanguage.tvTitle.text = getString(R.string.txt_language)
            toolBarLanguage.btnBack.setOnClickListener { onBack() }
            toolBarLanguage.btnAction.gone()
            rclLanguage.adapter = mLanguageAdapter
            mLanguageAdapter.setOnItemClick { _, position ->
                initNativeAd2()
                prLoading.visible()
                toolBarLanguage.btnSelect.invisible()

                mLanguageAdapter.selectItem(position)

                lifecycleScope.launch {
                    delay(3000.milliseconds)
                    prLoading.gone()
                    toolBarLanguage.btnSelect.visible()
                }
            }
        }
    }

    override fun initData() {
        viewModel.loadListLanguage()
    }

    override fun initObserver() {
        viewModel.listLanguage.observe(this) { list ->
            if (!fromSplash) {
                val currentCode = SystemUtil.getLanguage(this)
                list.forEach { model ->
                    model.selected = model.languageCode == currentCode
                }
            }
            mLanguageAdapter.setData(list)
        }
    }

    private fun initNativeAd1() {
        val adUnit =
            if (isCheckOpenApp) remoteConfig.native_language_second_1 else remoteConfig.native_language_first_1
        if (!adUnit.enabled) return binding.vNativeLanguage.frAdsNative.gone()
        Log.i("TAG_language", "initNativeAd1: adUnit $adUnit ")
        NativeAdsUtil.loadNativeLanguage1(isCheckOpenApp) { nativeAd ->
            nativeAd.getNativeAdLive().observeOnce(this@LanguageActivity) {
                if (nativeAd.available()) {
                    binding.vNativeLanguage.frAdsNative.visible()
                    nativeAd.showNative(
                        binding.vNativeLanguage.frAdsNative,
                        R.id.native_ad_view3,
                        null
                    )
                } else {
                    binding.vNativeLanguage.frAdsNative.gone()
                }
            }
        }
    }

    private fun initNativeAd2() {
        val adUnit =
            if (isCheckOpenApp) remoteConfig.native_language_second_2 else remoteConfig.native_language_first_2
        if (!adUnit.enabled) return binding.vNativeLanguage.frAdsNative2.gone()
        NativeAdsUtil.loadNativeLanguage2(isCheckOpenApp) { nativeAd ->
            nativeAd.getNativeAdLive().observeOnce(this@LanguageActivity) {
                if (nativeAd.available()) {
                    binding.vNativeLanguage.frAdsNative2.visible()
                    binding.vNativeLanguage.frAdsNative.gone()
                    nativeAd.showNative(
                        binding.vNativeLanguage.frAdsNative2,
                        R.id.native_ad_view1,
                        null
                    )
                } else {
                    binding.vNativeLanguage.frAdsNative2.gone()
                }
            }
        }
    }
}