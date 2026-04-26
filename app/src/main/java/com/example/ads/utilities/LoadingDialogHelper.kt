package com.example.ads.utilities

import com.example.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.example.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.example.ads.ui.dialog.DialogLoadingAds

/**
 * Helper to wrap ad load callbacks with loading dialog management
 */
object LoadingDialogHelper {

    /**
     * Wrap InterstitialOnLoadCallBack to show/dismiss loading dialog
     */
    fun wrapInterstitialCallback(
        dialog: DialogLoadingAds?,
        callback: InterstitialOnLoadCallBack?
    ): InterstitialOnLoadCallBack {
        return object : InterstitialOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean) {
                dialog?.dismiss()
                callback?.onResponse(successfullyLoaded)
            }
        }
    }

    /**
     * Wrap RewardedOnLoadCallBack to show/dismiss loading dialog
     */
    fun wrapRewardedCallback(
        dialog: DialogLoadingAds?,
        callback: RewardedOnLoadCallBack?
    ): RewardedOnLoadCallBack {
        return object : RewardedOnLoadCallBack {
            override fun onResponse(isSuccess: Boolean) {
                dialog?.dismiss()
                callback?.onResponse(isSuccess)
            }
        }
    }
}
