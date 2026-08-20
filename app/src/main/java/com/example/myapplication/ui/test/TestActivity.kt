package com.example.myapplication.ui.test

import android.util.Log
import com.example.myapplication.ads.AdInterstitialUtils
import com.example.myapplication.ads.AdNativeUtils
import com.example.myapplication.ads.AdRewardUtils
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityTestBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestActivity : BaseActivity<ActivityTestBinding>(ActivityTestBinding::inflate) {

    override fun initView() {
        binding.tvPreload2f.setOnClickListener {
            updateStatus("Preloading 2-Floor Interstitial and Rewarded...")
            AdInterstitialUtils.preloadTwoFloor(TEST_INTER_PLACEMENT)
            AdRewardUtils.preloadRewardedTwoFloor(TEST_REWARD_PLACEMENT)
            showToast("Started preloading 2-Floor Ads")
        }

        binding.tvShowInterWith2f.setOnClickListener {
            updateStatus("Requesting 2-Floor Interstitial ($TEST_INTER_PLACEMENT)...")
            AdInterstitialUtils.showTwoFloor(
                activity = this,
                placementName = TEST_INTER_PLACEMENT,
                showLoadingWhenNotReady = true
            ) {
                updateStatus("2-Floor Interstitial closed or failed -> Next action executed!")
                showToast("Interstitial 2F: Next Action Executed")
            }
        }

        binding.tvShowRewardWith2f.setOnClickListener {
            updateStatus("Requesting 2-Floor Rewarded ($TEST_REWARD_PLACEMENT)...")
            AdRewardUtils.showRewardedTwoFloor(
                activity = this,
                placementName = TEST_REWARD_PLACEMENT,
                showLoadingWhenNotReady = true,
                onRewardEarned = { amount, type ->
                    updateStatus("Reward earned: $amount $type")
                    showToast("Reward earned: $amount $type")
                },
                onDismissed = {
                    updateStatus("2-Floor Rewarded dismissed")
                    showToast("Rewarded 2F Dismissed")
                },
                onFailed = { message ->
                    updateStatus("2-Floor Rewarded failed: $message")
                    showToast("Rewarded 2F Failed: $message")
                }
            )
        }

        binding.tvShowNativeWith2f.setOnClickListener {
            updateStatus("Loading 2-Floor Native into container ($TEST_NATIVE_PLACEMENT)...")
            AdNativeUtils.showNativeTwoFloor(
                context = this,
                nativeContainer = binding.frNative,
                placementName = TEST_NATIVE_PLACEMENT,
                onFailure = { message ->
                    updateStatus("2-Floor Native failed: $message")
                    showToast("Native 2F Failed: $message")
                }
            )
        }
    }

    override fun initData() {
        updateStatus("TestActivity initialized. Ready to test 2-Floor Ads.")
    }

    private fun updateStatus(status: String) {
        Log.d(TAG, status)
        binding.tvStatus.text = "Status: $status"
    }

    companion object {
        private const val TAG = "TestActivity"
        private const val TEST_INTER_PLACEMENT = "inter_feature_first"
        private const val TEST_REWARD_PLACEMENT = "reward_feature"
        private const val TEST_NATIVE_PLACEMENT = "native_feature_first"
    }
}
