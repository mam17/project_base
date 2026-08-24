package com.example.myapplication.ads

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.libads.core.AdManager
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdAppOpenManager {
    private const val TAG = "AdAppOpenManager"

    private var isShowingAppOpen = false
    private var isLoadingAppOpen = false

    private val defaultPlacement = AdPlacement.APP_OPEN_RESUME

    // ════════════════════════════════════════════════════════════════
    //  Preload
    // ════════════════════════════════════════════════════════════════

    fun preload(placement: AdPlacement = defaultPlacement) {
        val adUnit = AdUnits.getUnit(placement.name, placement.type) ?: return
        if (isLoadingAppOpen || AdManager.getInstance().isReady(adUnit)) return

        isLoadingAppOpen = true
        Log.d(TAG, "preload: start '${adUnit.id}'")
        AdManager.getInstance().preload(adUnit) { result ->
            isLoadingAppOpen = false
            when (result) {
                is AdResult.Success -> Log.d(TAG, "preload: loaded '${adUnit.id}'")
                is AdResult.Failure -> Log.e(TAG, "preload: failed '${adUnit.id}' ${result.errorCode} ${result.message}")
                is AdResult.TimedOut -> Log.w(TAG, "preload: timed out '${adUnit.id}'")
            }
        }
    }

    fun preloadTwoFloor(placement: AdPlacement = defaultPlacement) {
        Ads.preload(placement)
    }

    // ════════════════════════════════════════════════════════════════
    //  Show
    // ════════════════════════════════════════════════════════════════

    fun show(
        activity: FragmentActivity,
        placement: AdPlacement = defaultPlacement,
        action: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            action?.invoke()
            return
        }

        if (placement.useTwoFloor) {
            showTwoFloor(activity, placement, action)
        } else {
            val adUnit = AdUnits.getUnit(placement.name, placement.type)
            if (adUnit == null) {
                action?.invoke()
                return
            }
            showInternal(action) { callback ->
                AdManager.getInstance().show(activity, adUnit, callback)
            }
        }
    }

    fun show(
        fragment: Fragment,
        placement: AdPlacement = defaultPlacement,
        action: (() -> Unit)? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached ||
            activity.isFinishing || activity.isDestroyed
        ) {
            action?.invoke()
            return
        }

        if (placement.useTwoFloor) {
            showTwoFloor(fragment, placement, action)
        } else {
            val adUnit = AdUnits.getUnit(placement.name, placement.type)
            if (adUnit == null) {
                action?.invoke()
                return
            }
            showInternal(action) { callback ->
                AdManager.getInstance().show(fragment, adUnit, callback)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal
    // ════════════════════════════════════════════════════════════════

    private fun showTwoFloor(
        activity: FragmentActivity,
        placement: AdPlacement,
        action: (() -> Unit)?
    ) {
        val units = AdUnits.getTwoFloor(placement.name, placement.type)
        val adManager = AdManager.getInstance()
        val targetUnit = when {
            units.unit2f != null && adManager.isReady(units.unit2f) -> units.unit2f
            units.unitBase != null && adManager.isReady(units.unitBase) -> units.unitBase
            units.unit2f != null -> units.unit2f
            else -> units.unitBase
        }

        if (targetUnit == null) {
            action?.invoke()
            return
        }

        showInternal(action) { callback ->
            adManager.show(activity, targetUnit, callback)
        }
    }

    private fun showTwoFloor(
        fragment: Fragment,
        placement: AdPlacement,
        action: (() -> Unit)?
    ) {
        val units = AdUnits.getTwoFloor(placement.name, placement.type)
        val adManager = AdManager.getInstance()
        val targetUnit = when {
            units.unit2f != null && adManager.isReady(units.unit2f) -> units.unit2f
            units.unitBase != null && adManager.isReady(units.unitBase) -> units.unitBase
            units.unit2f != null -> units.unit2f
            else -> units.unitBase
        }

        if (targetUnit == null) {
            action?.invoke()
            return
        }

        showInternal(action) { callback ->
            adManager.show(fragment, targetUnit, callback)
        }
    }

    private fun showInternal(action: (() -> Unit)?, showAction: (AdShowCallback) -> Unit) {
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
        Log.d(TAG, "show: displaying app open ad")
        showAction(object : AdShowCallback {
            override fun onAdShown() {
                Log.d(TAG, "show: shown")
            }

            override fun onAdDismissed() {
                Log.d(TAG, "show: dismissed")
                isShowingAppOpen = false
                preload()
                handleNext()
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                Log.e(TAG, "show: failed $errorCode $message")
                isShowingAppOpen = false
                preload()
                handleNext()
            }
        })
    }
}
