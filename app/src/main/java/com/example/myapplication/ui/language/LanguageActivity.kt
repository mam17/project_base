package com.example.myapplication.ui.language

import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.ads.AdPlacement
import com.example.myapplication.ads.Ads
import com.example.myapplication.ads.AdUnits
import com.example.myapplication.ads.NativeType
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityLanguageBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.onboarding.OnboardingActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val viewModel: LanguageViewModel by viewModels()
    private var mLanguageAdapter = LanguageAdapter()
    private var fromSplash = false
    private var isShowingType1 = false

    // Native Ad Placements
    private val nativeLanguageFirst1 = AdPlacement.native(AdUnits.NATIVE_LANGUAGE_FIRST_1, NativeType.TYPE_3)
    private val nativeLanguageFirst2 = AdPlacement.native(AdUnits.NATIVE_LANGUAGE_FIRST_2, NativeType.TYPE_1)
    private val nativeLanguageSecond1 = AdPlacement.native(AdUnits.NATIVE_LANGUAGE_SECOND_1, NativeType.TYPE_3)
    private val nativeLanguageSecond2 = AdPlacement.native(AdUnits.NATIVE_LANGUAGE_SECOND_2, NativeType.TYPE_1)

    private val currentNative1 get() = if (!isCheckOpenApp) nativeLanguageFirst1 else nativeLanguageSecond1
    private val currentNative2 get() = if (!isCheckOpenApp) nativeLanguageFirst2 else nativeLanguageSecond2

    override fun initView() {
        fromSplash = intent.getBooleanExtra(Constant.KEY_FROM_SPLASH, false)
        Log.i(TAG, "initUI: fromSplash $fromSplash")
        initUI()
        initListener()
    }

    private fun initListener() {
        binding.apply {
            toolBarLanguage.btnSelect.setOnClickListener {
                mLanguageAdapter.getSelectedModel()?.let { model ->
                    cleanAds()
                    SystemUtil.saveLanguage(this@LanguageActivity, model)
                    if (fromSplash) {
                        startNextActivity(OnboardingActivity::class.java, isFinish = true)
                    } else {
                        startActivityNewTask(MainActivity::class.java)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            toolBarLanguage.btnBack.isVisible = !fromSplash
            toolBarLanguage.tvTitle.text = getString(R.string.txt_language)
            toolBarLanguage.btnBack.setOnClickListener { onBack() }
            toolBarLanguage.btnSelect.visible()
            toolBarLanguage.btnAction.gone()
            rclLanguage.adapter = mLanguageAdapter
            mLanguageAdapter.setOnItemClick { _, position ->
                mLanguageAdapter.selectItem(position)
                switchToNativeType1()
            }
        }
    }

    private fun switchToNativeType1() {
        if (!isShowingType1) {
            isShowingType1 = true
            binding.vNativeLanguage.nativeAdView3.gone()
            binding.vNativeLanguage.nativeAdView1.visible()
            Ads.showInto(this, binding.vNativeLanguage.nativeAdView1, currentNative2)
        }
    }

    override fun initData() {
        viewModel.loadListLanguage()

        // 1. Show Native Type 3 first
        Ads.showInto(this, binding.vNativeLanguage.nativeAdView3, currentNative1)

        // 2. Preload Native Type 1 in background so it's ready upon language selection
        Ads.preload(currentNative2)
    }

    override fun initObserver() {
        viewModel.listLanguage.observe(this) { list ->
            val currentCode = SystemUtil.getLanguage(this)
            list.forEach { model ->
                model.selected = model.languageCode == currentCode
            }
            mLanguageAdapter.setData(list)
        }
    }

    private fun cleanAds() {
        Ads.destroy(nativeLanguageFirst1, nativeLanguageFirst2, nativeLanguageSecond1, nativeLanguageSecond2)
    }

    override fun onDestroy() {
        cleanAds()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TAG_LANGUAGE"
    }
}