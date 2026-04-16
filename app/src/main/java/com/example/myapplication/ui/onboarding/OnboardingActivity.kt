package com.example.myapplication.ui.onboarding

import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.ads.natives.presentation.enums.NativeAdKey
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel
import com.example.myapplication.ui.feature.FeatureActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity :
    BaseActivity<ActivityOnboardingBinding>(ActivityOnboardingBinding::inflate) {

    private val viewModel: OnboardingViewModel by viewModels()
    private val mAdapter = OnboardingAdapter()
    private var currentPosition = 0

    // Content pages sit at adapter positions 0, 2, 4 → map to their NativeAdKey
    private val contentNativeKeys = mapOf(
        0 to NativeAdKey.ON_BOARDING_1,
        2 to NativeAdKey.ON_BOARDING_2,
        4 to NativeAdKey.ON_BOARDING_3
    )
    private val loadedContentNativeAds = mutableMapOf<Int, NativeAd>()
    private val loadedFsNativeAds = mutableMapOf<NativeAdKey, NativeAd>()

    override fun initView() {
        setBaseFullScreen()
        mAdapter.onCloseClick = {
            if (currentPosition < mAdapter.itemCount - 1) {
                binding.vpOnBoarding.currentItem = currentPosition + 1
            }
        }

        binding.apply {
            vpOnBoarding.adapter = mAdapter
            dotIndicator.attachTo(vpOnBoarding)

            vpOnBoarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition = position
                    btnNext.setText(
                        if (position == mAdapter.itemCount - 1) R.string.txt_get_start
                        else R.string.txt_next
                    )
                    updateBottomNative(position)
                }
            })

            btnNext.setOnClickListener {
                if (currentPosition < mAdapter.itemCount - 1) {
                    vpOnBoarding.currentItem = currentPosition + 1
                } else {
//                    spManager.isCompletedOnboarding = true
//                    startActivityNewTask(MainActivity::class.java)
                    startNextActivity(FeatureActivity::class.java, isFinish = true)
                }
            }
        }
    }

    override fun initData() {
        viewModel.loadListOnBoarding()

        // Load bottom natives (content pages)
        contentNativeKeys.forEach { (position, key) ->
            loadNativeAd(key,
                onLoaded = { nativeAd ->
                    loadedContentNativeAds[position] = nativeAd
                    if (currentPosition == position) binding.nativeAdOnb.setNativeAd(nativeAd)
                }
            )
        }

        // Load full-screen natives (FS pages in adapter)
        loadNativeAd(NativeAdKey.ON_BOARDING_FS_1,
            onLoaded = { nativeAd ->
                loadedFsNativeAds[NativeAdKey.ON_BOARDING_FS_1] = nativeAd
                mAdapter.setNativeAd(NativeAdKey.ON_BOARDING_FS_1, nativeAd)
            }
        )
        loadNativeAd(NativeAdKey.ON_BOARDING_FS_2,
            onLoaded = { nativeAd ->
                loadedFsNativeAds[NativeAdKey.ON_BOARDING_FS_2] = nativeAd
                mAdapter.setNativeAd(NativeAdKey.ON_BOARDING_FS_2, nativeAd)
            }
        )
    }

    override fun initObserver() {
        viewModel.listOnBoarding.observe(this) { list ->
            mAdapter.submitList(buildAdapterItems(list))

            // Apply any FS natives that loaded before setData was called
            loadedFsNativeAds.forEach { (key, nativeAd) ->
                mAdapter.setNativeAd(key, nativeAd)
            }

            // Show bottom native for first page if already loaded
            loadedContentNativeAds[0]?.let { binding.nativeAdOnb.setNativeAd(it) }
        }
    }

    private fun buildAdapterItems(list: List<OnboardingModel>): List<OnboardingAdapter.Item> {
        // Final order: [Content1, FS_1, Content2, FS_2, Content3]
        return buildList {
            if (list.isNotEmpty()) add(OnboardingAdapter.Item.Content(list[0]))
            add(OnboardingAdapter.Item.NativeFs(NativeAdKey.ON_BOARDING_FS_1))
            if (list.size > 1) add(OnboardingAdapter.Item.Content(list[1]))
            add(OnboardingAdapter.Item.NativeFs(NativeAdKey.ON_BOARDING_FS_2))
            if (list.size > 2) add(OnboardingAdapter.Item.Content(list[2]))
        }
    }

    private fun updateBottomNative(position: Int) {
        if (mAdapter.isContentPage(position)) {
            binding.rlDotNext.visible()
            binding.nativeAdOnb.visible()
            loadedContentNativeAds[position]?.let { binding.nativeAdOnb.setNativeAd(it) }
        } else {
            binding.rlDotNext.gone()
            binding.nativeAdOnb.gone()
        }
    }
}
