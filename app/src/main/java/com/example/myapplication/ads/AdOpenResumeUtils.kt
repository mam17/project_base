package com.example.myapplication.ads

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdOpenResumeUtils {
    private const val TAG = "AdOpenResumeUtils"
    private var isShowingAppOpen = false
    private var isLoadingAppOpen = false

    fun preloadAppOpenResume() {
        if (isLoadingAppOpen || AdManager.getInstance().isReady(AdUnits.appOpenResume)) return

        isLoadingAppOpen = true
        Log.d(TAG, "preloadAppOpenResume: start")
        AdManager.getInstance().preload(AdUnits.appOpenResume) { result ->
            isLoadingAppOpen = false
            when (result) {
                is AdResult.Success -> Log.d(TAG, "preloadAppOpenResume: loaded")
                is AdResult.Failure -> Log.e(
                    TAG,
                    "preloadAppOpenResume: failed ${result.errorCode} ${result.message}"
                )
                is AdResult.TimedOut -> Log.w(TAG, "preloadAppOpenResume: timed out")
            }
        }
    }

    fun showAppOpenResume(activity: FragmentActivity, action: (() -> Unit)? = null) {
        showAppOpenResume(action) { callback ->
            AdManager.getInstance().show(activity, AdUnits.appOpenResume, callback)
        }
    }

    fun showAppOpenResume(fragment: Fragment, action: (() -> Unit)? = null) {
        if (!fragment.isAdded) {
            action?.invoke()
            return
        }
        showAppOpenResume(action) { callback ->
            AdManager.getInstance().show(fragment, AdUnits.appOpenResume, callback)
        }
    }

    private fun showAppOpenResume(action: (() -> Unit)?, showAction: (AdShowCallback) -> Unit) {
        if (isShowingAppOpen) {
            action?.invoke()
            return
        }

        if (!AdManager.getInstance().isReady(AdUnits.appOpenResume)) {
            Log.d(TAG, "showAppOpenResume: not ready")
            preloadAppOpenResume()
            action?.invoke()
            return
        }

        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                action?.invoke()
            }
        }

        isShowingAppOpen = true
        Log.d(TAG, "showAppOpenResume: show")
        showAction(object : AdShowCallback {
            override fun onAdShown() {
                Log.d(TAG, "showAppOpenResume: shown")
            }

            override fun onAdDismissed() {
                Log.d(TAG, "showAppOpenResume: dismissed")
                isShowingAppOpen = false
                preloadAppOpenResume()
                handleNext()
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                Log.e(TAG, "showAppOpenResume: failed $errorCode $message")
                isShowingAppOpen = false
                preloadAppOpenResume()
                handleNext()
            }
        })
    }
}
