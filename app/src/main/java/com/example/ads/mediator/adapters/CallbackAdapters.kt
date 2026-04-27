package com.example.ads.mediator.adapters

import android.util.Log
import com.example.ads.appOpen.screen.callbacks.AppOpenOnLoadCallBack
import com.example.ads.appOpen.screen.callbacks.AppOpenOnShowCallBack
import com.example.ads.interstitial.callbacks.InterstitialOnLoadCallBack
import com.example.ads.interstitial.callbacks.InterstitialOnShowCallBack
import com.example.ads.mediator.callbacks.AdLoadCallback
import com.example.ads.mediator.callbacks.AdShowCallback
import com.example.ads.rewarded.callbacks.RewardedOnLoadCallBack
import com.example.ads.rewarded.callbacks.RewardedOnShowCallBack
import com.example.ads.ui.dialog.DialogLoadingAds

internal object CallbackAdapters {

    // ===== Interstitial Load Callbacks =====

    fun toInterstitialLoadCallback(callback: AdLoadCallback?): InterstitialOnLoadCallBack {
        return object : InterstitialOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean) {
                if (successfullyLoaded) {
                    callback?.onSuccess()
                } else {
                    callback?.onFailure()
                }
            }
        }
    }

    // ===== Interstitial Show Callbacks =====

    fun toInterstitialShowCallback(callback: AdShowCallback?): InterstitialOnShowCallBack {
        return object : InterstitialOnShowCallBack {
            override fun onAdDismissedFullScreenContent() {
                callback?.onAdDismissed()
            }

            override fun onAdFailedToShow() {
                callback?.onAdFailedToShow()
            }

            override fun onAdShowedFullScreenContent() {
                callback?.onAdShowed()
            }

            override fun onAdClicked() {
                callback?.onAdClicked()
            }

            override fun onAdImpression() {}
        }
    }

    // ===== Rewarded Load Callbacks =====

    fun toRewardedLoadCallback(callback: AdLoadCallback?): RewardedOnLoadCallBack {
        return object : RewardedOnLoadCallBack {
            override fun onResponse(isSuccess: Boolean) {
                if (isSuccess) {
                    callback?.onSuccess()
                } else {
                    callback?.onFailure()
                }
            }
        }
    }

    // ===== Rewarded Show Callbacks =====

    fun toRewardedShowCallback(callback: AdShowCallback?): RewardedOnShowCallBack {
        return object : RewardedOnShowCallBack {
            override fun onAdDismissedFullScreenContent() {
                callback?.onAdDismissed()
            }

            override fun onAdFailedToShow() {
                callback?.onAdFailedToShow()
            }

            override fun onAdShowedFullScreenContent() {
                callback?.onAdShowed()
            }

            override fun onUserEarnedReward() {
                callback?.onRewardEarned()
            }

            override fun onAdImpression() {}
        }
    }

    // ===== AppOpen Load Callbacks =====

    fun toAppOpenLoadCallback(callback: AdLoadCallback?): AppOpenOnLoadCallBack {
        return object : AppOpenOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean, errorMessage: String?) {
                if (successfullyLoaded) {
                    callback?.onSuccess()
                } else {
                    callback?.onFailure(errorMessage)
                }
            }
        }
    }

    // ===== AppOpen Show Callbacks =====

    fun toAppOpenShowCallback(callback: AdShowCallback?): AppOpenOnShowCallBack {
        return object : AppOpenOnShowCallBack {
            override fun onAdDismissedFullScreenContent() {
                callback?.onAdDismissed()
            }

            override fun onAdFailedToShow() {
                callback?.onAdFailedToShow()
            }

            override fun onAdShowedFullScreenContent() {
                callback?.onAdShowed()
            }

            override fun onAdClicked() {
                callback?.onAdClicked()
            }
        }
    }

    // ===== Dialog Management (Optional) =====

    fun dismissLoadingDialog(dialog: DialogLoadingAds?) {
        try {
            dialog?.dismiss()
        } catch (e: Exception) {
            Log.w("CallbackAdapters", "Error dismissing loading dialog", e)
        }
    }

    fun isDialogShowing(dialog: DialogLoadingAds?): Boolean {
        return try {
            dialog?.isShowing ?: false
        } catch (e: Exception) {
            false
        }
    }
}
