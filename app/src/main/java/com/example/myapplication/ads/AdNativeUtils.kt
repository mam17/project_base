package com.example.myapplication.ads

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.R
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import java.util.concurrent.atomic.AtomicBoolean

object AdNativeUtils {
    private const val ONE_SECOND_MS = 1_000L

    // ==========================================
    // Preload
    // ==========================================

    fun preload(placementName: String = AdUnits.NATIVE_FEATURE_FIRST) {
        val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
        preload(unit)
    }

    fun preload(adUnit: AdUnit) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadTwoFloor(placementName: String = AdUnits.NATIVE_FEATURE_FIRST) {
        val units = AdUnits.getTwoFloor(placementName, AdType.NATIVE)
        if (units.unit2f != null) {
            AdManager.getInstance().preload(units.unit2f) { result ->
                if (result !is AdResult.Success && units.unitBase != null) {
                    AdManager.getInstance().preload(units.unitBase)
                }
            }
        } else if (units.unitBase != null) {
            AdManager.getInstance().preload(units.unitBase)
        }
    }

    // ==========================================
    // Show (Context / Backward Compatibility)
    // ==========================================

    fun showNative(
        context: Context,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        if (context is FragmentActivity) {
            showNative(context, nativeContainer, placementName, loadingLayoutRes, onFailure)
        } else {
            val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
            nativeContainer.removeAllViews()
            val loadingView = LayoutInflater.from(context).inflate(loadingLayoutRes, nativeContainer, false)
            nativeContainer.addView(loadingView)
            renderNative(nativeContainer, unit) { result ->
                if (result is AdResult.Failure) {
                    nativeContainer.removeAllViews()
                    onFailure?.invoke(result.message)
                } else if (result is AdResult.TimedOut) {
                    nativeContainer.removeAllViews()
                    onFailure?.invoke("Native ad timed out")
                }
            }
        }
    }

    // ==========================================
    // Show (Activity)
    // ==========================================

    fun showNative(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
        showNative(activity, nativeContainer, unit, loadingLayoutRes, onFailure)
    }

    fun showNative(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        adUnit: AdUnit,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        if (isHostInvalid(activity, activity)) {
            onFailure?.invoke("Activity is finishing or destroyed")
            return
        }
        showNativeInternal(activity, activity, nativeContainer, adUnit, loadingLayoutRes, onFailure)
    }

    fun showNativeTwoFloor(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.NATIVE)
        showNativeTwoFloor(activity, nativeContainer, units, loadingLayoutRes, onFailure)
    }

    fun showNativeTwoFloor(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        if (isHostInvalid(activity, activity)) {
            onFailure?.invoke("Activity is finishing or destroyed")
            return
        }
        showNativeTwoFloorInternal(activity, activity, nativeContainer, twoFloorUnits, loadingLayoutRes, onFailure)
    }

    // ==========================================
    // Show (Fragment)
    // ==========================================

    fun showNative(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
        showNative(fragment, nativeContainer, unit, loadingLayoutRes, onFailure)
    }

    fun showNative(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        adUnit: AdUnit,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached || isHostInvalid(activity, fragment)) {
            onFailure?.invoke("Fragment is not attached or destroyed")
            return
        }
        showNativeInternal(activity, fragment.viewLifecycleOwnerOrFragment(), nativeContainer, adUnit, loadingLayoutRes, onFailure)
    }

    fun showNativeTwoFloor(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.NATIVE)
        showNativeTwoFloor(fragment, nativeContainer, units, loadingLayoutRes, onFailure)
    }

    fun showNativeTwoFloor(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached || isHostInvalid(activity, fragment)) {
            onFailure?.invoke("Fragment is not attached or destroyed")
            return
        }
        showNativeTwoFloorInternal(activity, fragment.viewLifecycleOwnerOrFragment(), nativeContainer, twoFloorUnits, loadingLayoutRes, onFailure)
    }

    // ==========================================
    // LoadAndShow (Fresh Load)
    // ==========================================

    fun loadAndShowNative(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
        loadAndShowNative(activity, nativeContainer, unit, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNative(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        adUnit: AdUnit,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        AdManager.getInstance().destroy(adUnit)
        showNative(activity, nativeContainer, adUnit, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNative(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
        loadAndShowNative(fragment, nativeContainer, unit, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNative(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        adUnit: AdUnit,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        AdManager.getInstance().destroy(adUnit)
        showNative(fragment, nativeContainer, adUnit, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNativeTwoFloor(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.NATIVE)
        loadAndShowNativeTwoFloor(activity, nativeContainer, units, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNativeTwoFloor(
        activity: FragmentActivity,
        nativeContainer: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        twoFloorUnits.unit2f?.let { AdManager.getInstance().destroy(it) }
        twoFloorUnits.unitBase?.let { AdManager.getInstance().destroy(it) }
        showNativeTwoFloor(activity, nativeContainer, twoFloorUnits, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNativeTwoFloor(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.NATIVE)
        loadAndShowNativeTwoFloor(fragment, nativeContainer, units, loadingLayoutRes, onFailure)
    }

    fun loadAndShowNativeTwoFloor(
        fragment: Fragment,
        nativeContainer: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        loadingLayoutRes: Int = R.layout.layout_native_ad_loading,
        onFailure: ((message: String) -> Unit)? = null
    ) {
        twoFloorUnits.unit2f?.let { AdManager.getInstance().destroy(it) }
        twoFloorUnits.unitBase?.let { AdManager.getInstance().destroy(it) }
        showNativeTwoFloor(fragment, nativeContainer, twoFloorUnits, loadingLayoutRes, onFailure)
    }

    // ==========================================
    // Countdown Native
    // ==========================================

    fun showNativeWithCountdown(
        rootView: View,
        nativeContainer: ViewGroup,
        loadingContainer: View,
        closeContainer: View,
        timeCountView: TextView,
        closeButton: View,
        countdownSeconds: Long,
        onClose: () -> Unit,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        onFailure: ((message: String) -> Unit)? = null
    ): CountDownTimer? {
        val lifecycleOwner = rootView.context as? LifecycleOwner ?: return null
        return showNativeWithCountdown(
            lifecycleOwner = lifecycleOwner,
            rootView = rootView,
            nativeContainer = nativeContainer,
            loadingContainer = loadingContainer,
            closeContainer = closeContainer,
            timeCountView = timeCountView,
            closeButton = closeButton,
            countdownSeconds = countdownSeconds,
            onClose = onClose,
            placementName = placementName,
            onFailure = onFailure
        )
    }

    fun showNativeWithCountdown(
        lifecycleOwner: LifecycleOwner,
        rootView: View,
        nativeContainer: ViewGroup,
        loadingContainer: View,
        closeContainer: View,
        timeCountView: TextView,
        closeButton: View,
        countdownSeconds: Long,
        onClose: () -> Unit,
        placementName: String = AdUnits.NATIVE_FEATURE_FIRST,
        onFailure: ((message: String) -> Unit)? = null
    ): CountDownTimer? {
        val unit = AdUnits.getUnit(placementName, AdType.NATIVE) ?: AdUnits.mainNative
        return showNativeWithCountdown(
            lifecycleOwner = lifecycleOwner,
            rootView = rootView,
            nativeContainer = nativeContainer,
            loadingContainer = loadingContainer,
            closeContainer = closeContainer,
            timeCountView = timeCountView,
            closeButton = closeButton,
            countdownSeconds = countdownSeconds,
            onClose = onClose,
            adUnit = unit,
            onFailure = onFailure
        )
    }

    fun showNativeWithCountdown(
        lifecycleOwner: LifecycleOwner,
        rootView: View,
        nativeContainer: ViewGroup,
        loadingContainer: View,
        closeContainer: View,
        timeCountView: TextView,
        closeButton: View,
        countdownSeconds: Long,
        onClose: () -> Unit,
        adUnit: AdUnit,
        onFailure: ((message: String) -> Unit)? = null
    ): CountDownTimer? {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            onFailure?.invoke("LifecycleOwner is destroyed")
            return null
        }

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

        renderNative(nativeContainer, adUnit) { result ->
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderNative
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

    // ==========================================
    // Internal Helpers
    // ==========================================

    private fun showNativeInternal(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        nativeContainer: ViewGroup,
        adUnit: AdUnit,
        loadingLayoutRes: Int,
        onFailure: ((message: String) -> Unit)?
    ) {
        nativeContainer.removeAllViews()
        val loadingView = LayoutInflater.from(context).inflate(loadingLayoutRes, nativeContainer, false)
        nativeContainer.addView(loadingView)

        val handled = AtomicBoolean(false)
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                handled.set(true)
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        renderNative(nativeContainer, adUnit) { result ->
            if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderNative
            if (result is AdResult.Failure) {
                nativeContainer.removeAllViews()
                onFailure?.invoke(result.message)
            } else if (result is AdResult.TimedOut) {
                nativeContainer.removeAllViews()
                onFailure?.invoke("Native ad timed out")
            }
        }
    }

    private fun showNativeTwoFloorInternal(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        nativeContainer: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        loadingLayoutRes: Int,
        onFailure: ((message: String) -> Unit)?
    ) {
        val unit2f = twoFloorUnits.unit2f
        val unitBase = twoFloorUnits.unitBase

        if (unit2f == null && unitBase == null) {
            onFailure?.invoke("No native ad unit configured for ${twoFloorUnits.placementName}")
            return
        }

        nativeContainer.removeAllViews()
        val loadingView = LayoutInflater.from(context).inflate(loadingLayoutRes, nativeContainer, false)
        nativeContainer.addView(loadingView)

        val handled = AtomicBoolean(false)
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                handled.set(true)
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (unit2f != null) {
            AdManager.getInstance().renderInto(nativeContainer, unit2f) { result2f ->
                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                when (result2f) {
                    is AdResult.Success -> {
                        // 2F rendered successfully
                    }
                    is AdResult.Failure, is AdResult.TimedOut -> {
                        if (unitBase != null) {
                            AdManager.getInstance().renderInto(nativeContainer, unitBase) { baseResult ->
                                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
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
                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
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

    private fun isHostInvalid(activity: FragmentActivity, lifecycleOwner: LifecycleOwner): Boolean {
        return activity.isFinishing || activity.isDestroyed || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED
    }

    private fun Fragment.viewLifecycleOwnerOrFragment(): LifecycleOwner {
        return runCatching { viewLifecycleOwner }.getOrDefault(this)
    }

    private fun renderNative(
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback
    ) {
        AdManager.getInstance().renderInto(container, adUnit, callback)
    }
}
