package com.example.myapplication.base_ads.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.example.myapplication.MyApplication
import com.example.myapplication.BuildConfig

object AdsEx {
    fun <T> LiveData<T>.observeOnce(
        lifecycleOwner: LifecycleOwner,
        observer: (T) -> Unit
    ) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T) {
                observer(t)
                removeObserver(this)
            }
        })
    }

    fun Context.logAdRevenue(
        adValue: AdValue,
        adUnitId: String = "",
        responseInfo: ResponseInfo? = null,
        adType: String = "unknown"
    ) {
        MmpAdEventTracker.logImpression(
            context = this,
            adType = adType,
            placement = adUnitId,
            responseInfo = responseInfo
        )
        (applicationContext as? MyApplication)?.handleAdRevenue(adValue, adUnitId, responseInfo, adType)
    }

    const val ID_INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"
    const val ID_BANNER_TEST = "ca-app-pub-3940256099942544/2014213617"
    const val ID_REWARD_TEST = "ca-app-pub-3940256099942544/5224354917"

    const val ID_APP_OPEN_TEST = "ca-app-pub-3940256099942544/9257395921"
    const val ID_NATIVE_TEST = "ca-app-pub-3940256099942544/2247696110"
    fun getNativeId(realId: String): String {
        return if (BuildConfig.DEBUG ) {
            Log.d("TAG_ADS_AdsEx", "Using Test Native ID (Debug or Empty Real ID)")
            ID_NATIVE_TEST
        } else {
            realId
        }
    }

    fun getBannerId(realId: String): String {
        return if (BuildConfig.DEBUG ) {
            Log.d("TAG_ADS_AdsEx", "Using Test Banner ID (Debug or Empty Real ID)")
            ID_BANNER_TEST
        } else realId
    }

    fun getInterstitialId(realId: String): String {
        return if (BuildConfig.DEBUG ) {
            Log.d("TAG_ADS_AdsEx", "Using Test Interstitial ID (Debug or Empty Real ID)")
            ID_INTERSTITIAL_TEST
        } else realId
    }

    fun getRewardId(realId: String): String {
        return if (BuildConfig.DEBUG ) {
            Log.d("TAG_ADS_AdsEx", "Using Test Reward ID (Debug or Empty Real ID)")
            ID_REWARD_TEST
        } else realId
    }


    fun getAppOpenId(realId: String): String {
        return if (BuildConfig.DEBUG ) {
            Log.d("TAG_ADS_AdsEx", "Using Test AppOpen ID (Debug or Empty Real ID)")
            ID_APP_OPEN_TEST
        } else {
            realId
        }
    }
}
