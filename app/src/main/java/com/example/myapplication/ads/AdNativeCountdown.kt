package com.example.myapplication.ads

import android.graphics.Color
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.callback.AdResult
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles the specialized "native ad with countdown close button" flow.
 *
 * This is intentionally kept separate from [Ads] because it involves complex
 * UI state management (countdown timer, loading/close visibility toggling)
 * that doesn't fit the generic preload/show pattern.
 *
 * Usage:
 * ```
 * nativeCloseTimer = AdNativeCountdown.show(
 *     lifecycleOwner = this,
 *     rootView = binding.root,
 *     nativeContainer = binding.nativeContainer,
 *     loadingContainer = binding.loadingContainer,
 *     closeContainer = binding.closeContainer,
 *     timeCountView = binding.tvTimeCount,
 *     closeButton = binding.btnClose,
 *     countdownSeconds = 3L,
 *     onClose = { navigateNext() },
 *     placement = AdPlacement.native("native_feature_first")
 * )
 * ```
 */
object AdNativeCountdown {
    private const val ONE_SECOND_MS = 1_000L

    fun show(
        lifecycleOwner: LifecycleOwner,
        rootView: View,
        nativeContainer: ViewGroup,
        loadingContainer: View,
        closeContainer: View,
        timeCountView: TextView,
        closeButton: View,
        countdownSeconds: Long,
        onClose: () -> Unit,
        placement: AdPlacement = AdPlacement.NATIVE_FEATURE,
        onFailure: ((message: String) -> Unit)? = null
    ): CountDownTimer? {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            onFailure?.invoke("LifecycleOwner is destroyed")
            return null
        }

        // Setup initial UI state
        rootView.visibility = View.VISIBLE
        rootView.setBackgroundColor(Color.WHITE)
        nativeContainer.visibility = View.GONE
        loadingContainer.visibility = View.VISIBLE
        closeContainer.visibility = View.VISIBLE
        timeCountView.visibility = View.VISIBLE
        closeButton.visibility = View.GONE
        timeCountView.text = countdownSeconds.toString()

        val isClosed = AtomicBoolean(false)
        val handleClose = {
            if (isClosed.compareAndSet(false, true)) {
                rootView.visibility = View.GONE
                onClose()
            }
        }

        closeButton.setOnClickListener { handleClose() }

        var timer: CountDownTimer? = null

        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                timer?.cancel()
                timer = null
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Resolve and render native ad
        val adUnit = AdUnits.getUnit(placement.name, AdType.NATIVE)
        if (adUnit == null) {
            loadingContainer.visibility = View.GONE
            onFailure?.invoke("No native AdUnit configured for '${placement.name}'")
            return null
        }

        AdManager.getInstance().renderInto(nativeContainer, adUnit) { result ->
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
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

        // Countdown timer
        val countDownTimer = object : CountDownTimer(countdownSeconds * ONE_SECOND_MS, ONE_SECOND_MS) {
            override fun onTick(millisUntilFinished: Long) {
                if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    cancel()
                    return
                }
                val secondsLeft = (millisUntilFinished / ONE_SECOND_MS).toInt() + 1
                timeCountView.text = secondsLeft.toString()
            }

            override fun onFinish() {
                if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return
                timeCountView.visibility = View.GONE
                closeButton.visibility = View.VISIBLE
            }
        }
        timer = countDownTimer
        return countDownTimer.start()
    }
}
