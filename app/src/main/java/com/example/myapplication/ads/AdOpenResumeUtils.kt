package com.example.myapplication.ads

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdOpenResumeUtils {
    private const val TAG = "AdOpenResumeUtils"
    private var isShowingAppOpen = false
    private var isLoadingAppOpen = false

    // ==========================================
    // Preload
    // ==========================================

    fun preloadAppOpenResume(placementName: String = AdUnits.APPOPEN_RESUME) {
        val unit = AdUnits.getUnit(placementName, AdType.APP_OPEN) ?: AdUnits.appOpenResume
        preloadAppOpenResume(unit)
    }

    fun preloadAppOpenResume(adUnit: AdUnit) {
        if (isLoadingAppOpen || AdManager.getInstance().isReady(adUnit)) return

        isLoadingAppOpen = true
        Log.d(TAG, "preloadAppOpenResume: start '${adUnit.id}'")
        AdManager.getInstance().preload(adUnit) { result ->
            isLoadingAppOpen = false
            when (result) {
                is AdResult.Success -> Log.d(TAG, "preloadAppOpenResume: loaded '${adUnit.id}'")
                is AdResult.Failure -> Log.e(
                    TAG,
                    "preloadAppOpenResume: failed '${adUnit.id}' ${result.errorCode} ${result.message}"
                )
                is AdResult.TimedOut -> Log.w(TAG, "preloadAppOpenResume: timed out '${adUnit.id}'")
            }
        }
    }

    fun preloadTwoFloor(placementName: String = AdUnits.APPOPEN_RESUME) {
        val units = AdUnits.getTwoFloor(placementName, AdType.APP_OPEN)
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
    // Show (Activity)
    // ==========================================

    fun showAppOpenResume(
        activity: FragmentActivity,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.APP_OPEN) ?: AdUnits.appOpenResume
        showAppOpenResume(activity, unit, action)
    }

    fun showAppOpenResume(
        activity: FragmentActivity,
        adUnit: AdUnit,
        action: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            action?.invoke()
            return
        }
        showAppOpenResume(action) { callback ->
            AdManager.getInstance().show(activity, adUnit, callback)
        }
    }

    fun showAppOpenResumeTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.APP_OPEN)
        showAppOpenResumeTwoFloor(activity, units, action)
    }

    fun showAppOpenResumeTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        action: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            action?.invoke()
            return
        }
        val unit2f = twoFloorUnits.unit2f
        val unitBase = twoFloorUnits.unitBase
        val targetUnit = when {
            unit2f != null && AdManager.getInstance().isReady(unit2f) -> unit2f
            unitBase != null && AdManager.getInstance().isReady(unitBase) -> unitBase
            unit2f != null -> unit2f
            else -> unitBase
        }

        if (targetUnit == null) {
            action?.invoke()
            return
        }

        showAppOpenResume(activity, targetUnit, action)
    }

    // ==========================================
    // Show (Fragment)
    // ==========================================

    fun showAppOpenResume(
        fragment: Fragment,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.APP_OPEN) ?: AdUnits.appOpenResume
        showAppOpenResume(fragment, unit, action)
    }

    fun showAppOpenResume(
        fragment: Fragment,
        adUnit: AdUnit,
        action: (() -> Unit)? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached || activity.isFinishing || activity.isDestroyed) {
            action?.invoke()
            return
        }
        showAppOpenResume(action) { callback ->
            AdManager.getInstance().show(fragment, adUnit, callback)
        }
    }

    fun showAppOpenResumeTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.APP_OPEN)
        showAppOpenResumeTwoFloor(fragment, units, action)
    }

    fun showAppOpenResumeTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        action: (() -> Unit)? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached || activity.isFinishing || activity.isDestroyed) {
            action?.invoke()
            return
        }
        val unit2f = twoFloorUnits.unit2f
        val unitBase = twoFloorUnits.unitBase
        val targetUnit = when {
            unit2f != null && AdManager.getInstance().isReady(unit2f) -> unit2f
            unitBase != null && AdManager.getInstance().isReady(unitBase) -> unitBase
            unit2f != null -> unit2f
            else -> unitBase
        }

        if (targetUnit == null) {
            action?.invoke()
            return
        }

        showAppOpenResume(fragment, targetUnit, action)
    }

    // ==========================================
    // LoadAndShow (Fresh Load)
    // ==========================================

    fun loadAndShowAppOpenResume(
        activity: FragmentActivity,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.APP_OPEN) ?: AdUnits.appOpenResume
        loadAndShowAppOpenResume(activity, unit, action)
    }

    fun loadAndShowAppOpenResume(
        activity: FragmentActivity,
        adUnit: AdUnit,
        action: (() -> Unit)? = null
    ) {
        AdManager.getInstance().destroy(adUnit)
        showAppOpenResume(activity, adUnit, action)
    }

    fun loadAndShowAppOpenResume(
        fragment: Fragment,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.APP_OPEN) ?: AdUnits.appOpenResume
        loadAndShowAppOpenResume(fragment, unit, action)
    }

    fun loadAndShowAppOpenResume(
        fragment: Fragment,
        adUnit: AdUnit,
        action: (() -> Unit)? = null
    ) {
        AdManager.getInstance().destroy(adUnit)
        showAppOpenResume(fragment, adUnit, action)
    }

    fun loadAndShowTwoFloor(
        activity: FragmentActivity,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.APP_OPEN)
        loadAndShowTwoFloor(activity, units, action)
    }

    fun loadAndShowTwoFloor(
        activity: FragmentActivity,
        twoFloorUnits: TwoFloorAdUnits,
        action: (() -> Unit)? = null
    ) {
        twoFloorUnits.unit2f?.let { AdManager.getInstance().destroy(it) }
        twoFloorUnits.unitBase?.let { AdManager.getInstance().destroy(it) }
        showAppOpenResumeTwoFloor(activity, twoFloorUnits, action)
    }

    fun loadAndShowTwoFloor(
        fragment: Fragment,
        placementName: String = AdUnits.APPOPEN_RESUME,
        action: (() -> Unit)? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.APP_OPEN)
        loadAndShowTwoFloor(fragment, units, action)
    }

    fun loadAndShowTwoFloor(
        fragment: Fragment,
        twoFloorUnits: TwoFloorAdUnits,
        action: (() -> Unit)? = null
    ) {
        twoFloorUnits.unit2f?.let { AdManager.getInstance().destroy(it) }
        twoFloorUnits.unitBase?.let { AdManager.getInstance().destroy(it) }
        showAppOpenResumeTwoFloor(fragment, twoFloorUnits, action)
    }

    // ==========================================
    // Internal Show Handler
    // ==========================================

    private fun showAppOpenResume(action: (() -> Unit)?, showAction: (AdShowCallback) -> Unit) {
        if (isShowingAppOpen) {
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
