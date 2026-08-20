package com.example.myapplication.ads

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.utils.firebase.FirebaseConfigManager
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult

object AdNativeUtils {
    private const val ONE_SECOND_MS = 1_000L

    fun showNative(
        context: Context,
        nativeContainer: ViewGroup,
        adUnit: AdUnit = AdUnits.mainNative,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        nativeContainer.removeAllViews()
        val loadingView = LayoutInflater.from(context)
            .inflate(loadingLayoutRes, nativeContainer, false)
        nativeContainer.addView(loadingView)

        renderNative(nativeContainer, adUnit) { result ->
            if (result is AdResult.Failure) {
                nativeContainer.removeAllViews()
                onFailure?.invoke(result.message)
            }
        }
    }

    fun showNativeTwoFloor(
        context: Context,
        nativeContainer: ViewGroup,
        placementName: String = "native_feature_first",
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val units = FirebaseConfigManager.instance().getTwoFloorAdUnits(placementName, AdType.NATIVE)
        showNativeTwoFloor(
            context = context,
            nativeContainer = nativeContainer,
            twoFloorUnits = units,
            loadingLayoutRes = loadingLayoutRes,
            onFailure = onFailure
        )
    }

    fun showNativeTwoFloor(
        context: Context,
        nativeContainer: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val unit2f = twoFloorUnits.unit2f
        val unitBase = twoFloorUnits.unitBase

        if (unit2f == null && unitBase == null) {
            onFailure?.invoke("No native ad unit configured for ${twoFloorUnits.placementName}")
            return
        }

        nativeContainer.removeAllViews()
        val loadingView = LayoutInflater.from(context)
            .inflate(loadingLayoutRes, nativeContainer, false)
        nativeContainer.addView(loadingView)

        if (unit2f != null) {
            AdManager.getInstance().renderInto(nativeContainer, unit2f) { result2f ->
                when (result2f) {
                    is AdResult.Success -> {
                        // Rendered 2F native ad
                    }
                    is AdResult.Failure, is AdResult.TimedOut -> {
                        if (unitBase != null) {
                            AdManager.getInstance().renderInto(nativeContainer, unitBase) { baseResult ->
                                if (baseResult is AdResult.Failure) {
                                    nativeContainer.removeAllViews()
                                    onFailure?.invoke(baseResult.message)
                                } else if (baseResult is AdResult.TimedOut) {
                                    nativeContainer.removeAllViews()
                                    onFailure?.invoke("Native base timed out")
                                }
                            }
                        } else {
                            nativeContainer.removeAllViews()
                            val msg = if (result2f is AdResult.Failure) result2f.message else "Native 2F timed out"
                            onFailure?.invoke(msg)
                        }
                    }
                }
            }
        } else if (unitBase != null) {
            AdManager.getInstance().renderInto(nativeContainer, unitBase) { result ->
                if (result is AdResult.Failure) {
                    nativeContainer.removeAllViews()
                    onFailure?.invoke(result.message)
                } else if (result is AdResult.TimedOut) {
                    nativeContainer.removeAllViews()
                    onFailure?.invoke("Native timed out")
                }
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
        adUnit: AdUnit = AdUnits.mainNative,
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

        renderNative(nativeContainer, adUnit) { result ->
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

    private fun renderNative(
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback
    ) {
        AdManager.getInstance().renderInto(container, adUnit, callback)
    }
}
