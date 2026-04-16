package com.example.myapplication.ui.feature

import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityFeatureBinding
import com.example.myapplication.ui.main.MainActivity

class FeatureActivity : BaseActivity<ActivityFeatureBinding>(ActivityFeatureBinding::inflate) {
    override fun initView() {
        binding.btnNext.setOnClickListener {
            spManager.isCompletedOnboarding = true
            startNextActivity(MainActivity::class.java, isFinish = true)
        }

    }

    override fun initData() {
        initFeatureNative()
    }

    private fun initFeatureNative() {
        loadNativeAd(
            NativeAdKey.ON_BOARDING_FS_1,
            onLoaded = { nativeAd ->
                binding.nativeAdFeature.setNativeAd(nativeAd)
            }
        )
    }
}