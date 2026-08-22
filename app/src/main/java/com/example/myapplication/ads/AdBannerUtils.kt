package com.example.myapplication.ads

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.CollapsiblePositionType
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import java.util.concurrent.atomic.AtomicBoolean

object AdBannerUtils {

    // ==========================================
    // Preload
    // ==========================================

    fun preload(
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.BANNER) ?: AdUnits.mainBanner
        preload(unit.copy(collapsiblePositionType = collapsiblePositionType))
    }

    fun preload(adUnit: AdUnit) {
        AdManager.getInstance().preload(adUnit)
    }

    fun preloadTwoFloor(
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.BANNER)
        val unit2f = units.unit2f?.copy(collapsiblePositionType = collapsiblePositionType)
        val unitBase = units.unitBase?.copy(collapsiblePositionType = collapsiblePositionType)

        if (unit2f != null) {
            AdManager.getInstance().preload(unit2f) { result ->
                if (result !is AdResult.Success && unitBase != null) {
                    AdManager.getInstance().preload(unitBase)
                }
            }
        } else if (unitBase != null) {
            AdManager.getInstance().preload(unitBase)
        }
    }

    // ==========================================
    // Show (Activity)
    // ==========================================

    fun showBanner(
        container: ViewGroup,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val context = container.context
        if (context is FragmentActivity) {
            showBanner(context, container, AdUnits.BANNER_COLLAP, collapsiblePositionType, callback)
        } else {
            val unit = (AdUnits.getUnit(AdUnits.BANNER_COLLAP, AdType.BANNER) ?: AdUnits.mainBanner)
                .copy(collapsiblePositionType = collapsiblePositionType)
            AdManager.getInstance().renderInto(container, unit, callback)
        }
    }

    fun showBanner(
        activity: FragmentActivity,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.BANNER) ?: AdUnits.mainBanner
        showBanner(activity, container, unit.copy(collapsiblePositionType = collapsiblePositionType), callback)
    }

    fun showBanner(
        activity: FragmentActivity,
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback? = null
    ) {
        if (isHostInvalid(activity, activity)) {
            callback?.onResult(AdResult.Failure(adUnit.id, -5, "Activity is finishing or destroyed"))
            return
        }
        showBannerInternal(activity, container, adUnit, callback)
    }

    fun showBannerTwoFloor(
        activity: FragmentActivity,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.BANNER)
        showBannerTwoFloor(activity, container, units, collapsiblePositionType, callback)
    }

    fun showBannerTwoFloor(
        activity: FragmentActivity,
        container: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        if (isHostInvalid(activity, activity)) {
            callback?.onResult(AdResult.Failure(twoFloorUnits.placementName, -5, "Activity is finishing or destroyed"))
            return
        }
        showBannerTwoFloorInternal(activity, container, twoFloorUnits, collapsiblePositionType, callback)
    }

    // ==========================================
    // Show (Fragment)
    // ==========================================

    fun showBanner(
        fragment: Fragment,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.BANNER) ?: AdUnits.mainBanner
        showBanner(fragment, container, unit.copy(collapsiblePositionType = collapsiblePositionType), callback)
    }

    fun showBanner(
        fragment: Fragment,
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached || isHostInvalid(activity, fragment)) {
            callback?.onResult(AdResult.Failure(adUnit.id, -5, "Fragment is not attached or destroyed"))
            return
        }
        showBannerInternal(fragment.viewLifecycleOwnerOrFragment(), container, adUnit, callback)
    }

    fun showBannerTwoFloor(
        fragment: Fragment,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.BANNER)
        showBannerTwoFloor(fragment, container, units, collapsiblePositionType, callback)
    }

    fun showBannerTwoFloor(
        fragment: Fragment,
        container: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val activity = fragment.activity
        if (activity == null || !fragment.isAdded || fragment.isDetached || isHostInvalid(activity, fragment)) {
            callback?.onResult(AdResult.Failure(twoFloorUnits.placementName, -5, "Fragment is not attached or destroyed"))
            return
        }
        showBannerTwoFloorInternal(fragment.viewLifecycleOwnerOrFragment(), container, twoFloorUnits, collapsiblePositionType, callback)
    }

    // ==========================================
    // LoadAndShow (Fresh Load)
    // ==========================================

    fun loadAndShowBanner(
        activity: FragmentActivity,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.BANNER) ?: AdUnits.mainBanner
        loadAndShowBanner(activity, container, unit.copy(collapsiblePositionType = collapsiblePositionType), callback)
    }

    fun loadAndShowBanner(
        activity: FragmentActivity,
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback? = null
    ) {
        AdManager.getInstance().destroy(adUnit)
        showBanner(activity, container, adUnit, callback)
    }

    fun loadAndShowBanner(
        fragment: Fragment,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val unit = AdUnits.getUnit(placementName, AdType.BANNER) ?: AdUnits.mainBanner
        loadAndShowBanner(fragment, container, unit.copy(collapsiblePositionType = collapsiblePositionType), callback)
    }

    fun loadAndShowBanner(
        fragment: Fragment,
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback? = null
    ) {
        AdManager.getInstance().destroy(adUnit)
        showBanner(fragment, container, adUnit, callback)
    }

    fun loadAndShowBannerTwoFloor(
        activity: FragmentActivity,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.BANNER)
        loadAndShowBannerTwoFloor(activity, container, units, collapsiblePositionType, callback)
    }

    fun loadAndShowBannerTwoFloor(
        activity: FragmentActivity,
        container: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        twoFloorUnits.unit2f?.let { AdManager.getInstance().destroy(it) }
        twoFloorUnits.unitBase?.let { AdManager.getInstance().destroy(it) }
        showBannerTwoFloor(activity, container, twoFloorUnits, collapsiblePositionType, callback)
    }

    fun loadAndShowBannerTwoFloor(
        fragment: Fragment,
        container: ViewGroup,
        placementName: String = AdUnits.BANNER_COLLAP,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        val units = AdUnits.getTwoFloor(placementName, AdType.BANNER)
        loadAndShowBannerTwoFloor(fragment, container, units, collapsiblePositionType, callback)
    }

    fun loadAndShowBannerTwoFloor(
        fragment: Fragment,
        container: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        callback: AdLoadCallback? = null
    ) {
        twoFloorUnits.unit2f?.let { AdManager.getInstance().destroy(it) }
        twoFloorUnits.unitBase?.let { AdManager.getInstance().destroy(it) }
        showBannerTwoFloor(fragment, container, twoFloorUnits, collapsiblePositionType, callback)
    }

    // ==========================================
    // Internal Helpers
    // ==========================================

    private fun showBannerInternal(
        lifecycleOwner: LifecycleOwner,
        container: ViewGroup,
        adUnit: AdUnit,
        callback: AdLoadCallback?
    ) {
        val handled = AtomicBoolean(false)
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                handled.set(true)
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        AdManager.getInstance().renderInto(container, adUnit) { result ->
            if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
            callback?.onResult(result)
        }
    }

    private fun showBannerTwoFloorInternal(
        lifecycleOwner: LifecycleOwner,
        container: ViewGroup,
        twoFloorUnits: TwoFloorAdUnits,
        collapsiblePositionType: CollapsiblePositionType,
        callback: AdLoadCallback?
    ) {
        val unit2f = twoFloorUnits.unit2f?.copy(collapsiblePositionType = collapsiblePositionType)
        val unitBase = twoFloorUnits.unitBase?.copy(collapsiblePositionType = collapsiblePositionType)

        if (unit2f == null && unitBase == null) {
            callback?.onResult(AdResult.Failure(twoFloorUnits.placementName, -14, "No AdUnit configured for placement ${twoFloorUnits.placementName}"))
            return
        }

        val handled = AtomicBoolean(false)
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                handled.set(true)
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (unit2f != null) {
            AdManager.getInstance().renderInto(container, unit2f) { result2f ->
                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                when (result2f) {
                    is AdResult.Success -> callback?.onResult(result2f)
                    is AdResult.Failure, is AdResult.TimedOut -> {
                        if (unitBase != null) {
                            AdManager.getInstance().renderInto(container, unitBase) { baseResult ->
                                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                                callback?.onResult(baseResult)
                            }
                        } else {
                            callback?.onResult(result2f)
                        }
                    }
                }
            }
        } else if (unitBase != null) {
            AdManager.getInstance().renderInto(container, unitBase) { result ->
                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                callback?.onResult(result)
            }
        }
    }

    private fun isHostInvalid(activity: FragmentActivity, lifecycleOwner: LifecycleOwner): Boolean {
        return activity.isFinishing || activity.isDestroyed || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED
    }

    private fun Fragment.viewLifecycleOwnerOrFragment(): LifecycleOwner {
        return runCatching { viewLifecycleOwner }.getOrDefault(this)
    }
}
