package com.example.myapplication.ads

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.R
import com.libads.core.callback.AdResult

object NativeAdUtils {
    private const val ONE_SECOND_MS = 1_000L

    fun showNative(
        context: Context,
        nativeContainer: ViewGroup,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        nativeContainer.removeAllViews()
        val loadingView = LayoutInflater.from(context)
            .inflate(loadingLayoutRes, nativeContainer, false)
        nativeContainer.addView(loadingView)

        AdMobAds.showNative(nativeContainer) { result ->
            if (result is AdResult.Failure) {
                nativeContainer.removeAllViews()
                onFailure?.invoke(result.message)
            }
        }
    }

    fun showNativeWithCountdown(
        rootView: View,
        nativeContainer: ViewGroup,
        loadingContainer: View,
        closeContainer: View,
        timeCountView: TextView,
        closeButton: View,
        countdownSeconds: Long,
        onClose: () -> Unit,
        onFailure: ((message: String) -> Unit)? = null
    ): CountDownTimer {
        rootView.visibility = View.VISIBLE
        rootView.setBackgroundColor(Color.WHITE)
        nativeContainer.visibility = View.GONE
        loadingContainer.visibility = View.VISIBLE
        closeContainer.visibility = View.VISIBLE
        timeCountView.visibility = View.VISIBLE
        closeButton.visibility = View.GONE
        timeCountView.text = countdownSeconds.toString()

        closeButton.setOnClickListener {
            rootView.visibility = View.GONE
            onClose()
        }

        AdMobAds.showNative(nativeContainer) { result ->
            when (result) {
                is AdResult.Success -> {
                    loadingContainer.visibility = View.GONE
                    nativeContainer.visibility = View.VISIBLE
                }

                is AdResult.Failure -> {
                    loadingContainer.visibility = View.GONE
                    onFailure?.invoke(result.message)
                }

                is AdResult.TimedOut -> {
                    loadingContainer.visibility = View.GONE
                    onFailure?.invoke("Native timed out")
                }
            }
        }

        return object : CountDownTimer(countdownSeconds * ONE_SECOND_MS, ONE_SECOND_MS) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / ONE_SECOND_MS).toInt() + 1
                timeCountView.text = secondsLeft.toString()
            }

            override fun onFinish() {
                timeCountView.visibility = View.GONE
                closeButton.visibility = View.VISIBLE
            }
        }.start()
    }
}
