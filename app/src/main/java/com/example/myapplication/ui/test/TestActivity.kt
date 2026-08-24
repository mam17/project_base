package com.example.myapplication.ui.test

import android.util.Log
import com.example.myapplication.ads.AdPlacement
import com.example.myapplication.ads.Ads
import com.example.myapplication.ads.AdUnits
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityTestBinding
import com.libads.core.callback.AdResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestActivity : BaseActivity<ActivityTestBinding>(ActivityTestBinding::inflate) {

    override fun initView() {
        binding.tvPreload2f.setOnClickListener {
            updateStatus("Preloading 2-Floor Interstitial and Rewarded...")
            Ads.preload(
                AdPlacement.interstitial(TEST_INTER_PLACEMENT),
                AdPlacement.rewarded(TEST_REWARD_PLACEMENT)
            )
            showToast("Started preloading 2-Floor Ads")
        }

        binding.tvShowInterWith2f.setOnClickListener {
            updateStatus("Requesting 2-Floor Interstitial ($TEST_INTER_PLACEMENT)...")
            Ads.show(this, AdPlacement.interstitial(TEST_INTER_PLACEMENT)) {
                action {
                    updateStatus("2-Floor Interstitial closed or failed -> Next action executed!")
                    showToast("Interstitial 2F: Next Action Executed")
                }
            }
        }

        binding.tvShowRewardWith2f.setOnClickListener {
            updateStatus("Requesting 2-Floor Rewarded ($TEST_REWARD_PLACEMENT)...")
            Ads.show(this, AdPlacement.rewarded(TEST_REWARD_PLACEMENT)) {
                onRewardEarned { amount, type ->
                    updateStatus("Reward earned: $amount $type")
                    showToast("Reward earned: $amount $type")
                }
                onDismissed {
                    updateStatus("2-Floor Rewarded dismissed")
                    showToast("Rewarded 2F Dismissed")
                }
                onFailed { message ->
                    updateStatus("2-Floor Rewarded failed: $message")
                    showToast("Rewarded 2F Failed: $message")
                }
            }
        }

        binding.tvShowNativeWith2f.setOnClickListener {
            updateStatus("Loading 2-Floor Native into container ($TEST_NATIVE_PLACEMENT)...")
            Ads.showInto(
                host = this,
                container = binding.frNative,
                placement = AdPlacement.native(TEST_NATIVE_PLACEMENT)
            ) { result ->
                if (result is AdResult.Failure) {
                    updateStatus("2-Floor Native failed: ${result.message}")
                    showToast("Native 2F Failed: ${result.message}")
                } else if (result is AdResult.TimedOut) {
                    updateStatus("2-Floor Native timed out")
                    showToast("Native 2F Timed Out")
                }
            }
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
        private const val TEST_INTER_PLACEMENT = AdUnits.INTER_FEATURE_FIRST
        private const val TEST_REWARD_PLACEMENT = AdUnits.REWARD_FEATURE
        private const val TEST_NATIVE_PLACEMENT = AdUnits.NATIVE_FEATURE_FIRST
    }
}
