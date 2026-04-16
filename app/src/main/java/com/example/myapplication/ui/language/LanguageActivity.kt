package com.example.myapplication.ui.language

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityLanguageBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.onboarding.OnboardingActivity
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.invisible
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val viewModel: LanguageViewModel by viewModels()
    private var mLanguageAdapter = LanguageAdapter()
    private var isLanguageSelected = false

    override fun initView() {
        initUI()
        initListener()
    }

    private fun initListener() {
        binding.toolBarLanguage.btnSelect.setOnClickListener {
            mLanguageAdapter.getSelectedModel()?.let { model ->
                SystemUtil.saveLanguage(this, model)
                if (isLanguageSelected) {
                    startNextActivity(MainActivity::class.java, isFinish = true)
                } else {
                    startNextActivity(OnboardingActivity::class.java, isFinish = true)
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            toolBarLanguage.btnBack.setOnClickListener { onBack() }
            toolBarLanguage.btnSelect.visible()
            toolBarLanguage.btnAction.gone()
            rclLanguage.adapter = mLanguageAdapter
            mLanguageAdapter.setOnItemClick { _, position ->
                toolBarLanguage.btnSelect.invisible()
                prLoading.visible()
                mLanguageAdapter.selectItem(position)
                if (!isLanguageSelected) {
                    isLanguageSelected = true
                    loadLanguage2Native()
                }

                lifecycleScope.launch {
                    delay(3000)
                    prLoading.gone()
                    toolBarLanguage.btnSelect.visible()
                }
            }
        }
    }

    override fun initData() {
        viewModel.loadListLanguage()
        loadNativeAd(
            key = NativeAdKey.LANGUAGE_1,
            onLoaded = { nativeAd ->
                if (!isLanguageSelected) {
                    binding.frNativeLang.adNativeType3.setNativeAd(nativeAd)
                }
            },
            onFailed = {
                if (!isLanguageSelected) binding.frNativeLang.adNativeType3.gone()
            }
        )
    }

    private fun loadLanguage2Native() {
        loadNativeAd(
            key = NativeAdKey.LANGUAGE_2,
            onLoaded = { nativeAd ->
                binding.frNativeLang.adNativeType3.gone()
                binding.frNativeLang.adNativeLarge.visible()
                binding.frNativeLang.adNativeLarge.setNativeAd(nativeAd)
            },
            onFailed = {
                binding.frNativeLang.adNativeType3.gone()
            }
        )
    }

    override fun initObserver() {
        viewModel.listLanguage.observe(this) { list ->
            val currentCode = SystemUtil.getLanguage(this)
            list.forEach { model -> model.selected = model.languageCode == currentCode }
            mLanguageAdapter.setData(list)
        }
    }
}