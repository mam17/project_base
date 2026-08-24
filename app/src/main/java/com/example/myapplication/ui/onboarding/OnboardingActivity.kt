package com.example.myapplication.ui.onboarding

import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.ads.AdPlacement
import com.example.myapplication.ads.Ads
import com.example.myapplication.ads.AdUnits
import com.example.myapplication.ads.NativeType
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel
import com.example.myapplication.domain.layer.OnboardingModel.Companion.FULL_NATIVE_FLAG
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity :
    BaseActivity<ActivityOnboardingBinding>(ActivityOnboardingBinding::inflate) {
    private val viewModel: OnboardingViewModel by viewModels()
    private lateinit var mAdapter: OnboardingAdapter
    private var currentPosition = 0

    // Bottom Native Placements (Type 1)
    private val nativeObFirst1 = AdPlacement.native(AdUnits.NATIVE_OB_FIRST_1, NativeType.TYPE_1)
    private val nativeObSecond1 = AdPlacement.native(AdUnits.NATIVE_OB_SECOND_1, NativeType.TYPE_1)

    // Full Screen Native Placements
    private val nativeFsFirst1 = AdPlacement.native(AdUnits.NATIVE_FS_FIRST_1, NativeType.FULL_SCREEN)
    private val nativeFsFirst2 = AdPlacement.native(AdUnits.NATIVE_FS_FIRST_2, NativeType.FULL_SCREEN)
    private val nativeFsSecond1 = AdPlacement.native(AdUnits.NATIVE_FS_SECOND_1, NativeType.FULL_SCREEN)
    private val nativeFsSecond2 = AdPlacement.native(AdUnits.NATIVE_FS_SECOND_2, NativeType.FULL_SCREEN)

    private val currentNativeOb get() = if (isCheckOpenApp) nativeObSecond1 else nativeObFirst1
    private val currentNativeFs1 get() = if (isCheckOpenApp) nativeFsSecond1 else nativeFsFirst1
    private val currentNativeFs2 get() = if (isCheckOpenApp) nativeFsSecond2 else nativeFsFirst2

    override fun initView() {
        mAdapter = OnboardingAdapter(this) { adPosition ->
            if (adPosition < mAdapter.itemCount - 1) {
                binding.vpOnBoarding.currentItem = adPosition + 1
            } else {
                goToMain()
            }
        }

        binding.apply {
            vpOnBoarding.adapter = mAdapter
            dotIndicator.attachTo(vpOnBoarding)

            vpOnBoarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition = position
                    initAdsFullOnBoarding()
                    if (position == mAdapter.itemCount - 1) {
                        btnNext.setText(R.string.txt_get_start)
                    } else {
                        btnNext.setText(R.string.txt_next)
                    }
                }
            })

            btnNext.setOnClickListener {
                if (currentPosition < mAdapter.itemCount - 1) {
                    vpOnBoarding.currentItem = currentPosition + 1
                } else {
                    goToMain()
                }
            }
        }
    }

    override fun initData() {
        viewModel.loadListOnBoarding()

        // 1. Show bottom native ad (Type 1) into frNativeOnb
        Ads.showInto(this, binding.frNativeOnb, currentNativeOb)

        // 2. Preload full-screen native ads if enabled/available
        if (isAdAvailable(currentNativeFs1)) {
            Ads.preload(currentNativeFs1)
        }
        if (isAdAvailable(currentNativeFs2)) {
            Ads.preload(currentNativeFs2)
        }
    }

    override fun initObserver() {
        viewModel.listOnBoarding.observe(this) { baseList ->
            val fullList = ArrayList<OnboardingModel>(baseList)

            // Insert full-screen native ad 1 at index 1 (if available)
            if (isAdAvailable(currentNativeFs1) && fullList.isNotEmpty()) {
                fullList.add(
                    1,
                    OnboardingModel(
                        resImage = FULL_NATIVE_FLAG,
                        resTitle = 0,
                        resDescription = 0,
                        isNativeAd = true,
                        nativePlacement = currentNativeFs1
                    )
                )
            }

            // Insert full-screen native ad 2 at index 3 (if available)
            if (isAdAvailable(currentNativeFs2) && fullList.size >= 3) {
                fullList.add(
                    3,
                    OnboardingModel(
                        resImage = FULL_NATIVE_FLAG,
                        resTitle = 0,
                        resDescription = 0,
                        isNativeAd = true,
                        nativePlacement = currentNativeFs2
                    )
                )
            }

            Log.d(TAG, "Onboarding list populated with ${fullList.size} items (Base: ${baseList.size})")
            mAdapter.submitList(fullList) {
                initAdsFullOnBoarding()
            }
        }
    }

    private fun initAdsFullOnBoarding() = binding.apply {
        val list = mAdapter.currentList
        if (list.isEmpty() || currentPosition !in list.indices) return@apply

        dotIndicator.visible()
        btnNext.visible()
        navigationLayout.visible()

        when (list[currentPosition].resImage) {
            FULL_NATIVE_FLAG -> {
                Log.i(TAG, "initAdsFullOnBoarding: native full at position $currentPosition")
                dotIndicator.gone()
                btnNext.gone()
                navigationLayout.gone()
            }
        }
    }

    private fun isAdAvailable(placement: AdPlacement): Boolean {
        return AdUnits.getUnit(placement.name, placement.type) != null
    }

    private fun goToMain() {
        cleanAds()
        spManager.isCompletedOnboarding = true
        startActivityNewTask(MainActivity::class.java)
    }

    private fun cleanAds() {
        Ads.destroy(
            nativeObFirst1,
            nativeObSecond1,
            nativeFsFirst1,
            nativeFsFirst2,
            nativeFsSecond1,
            nativeFsSecond2
        )
    }

    override fun onDestroy() {
        cleanAds()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OnboardingActivity"
    }
}