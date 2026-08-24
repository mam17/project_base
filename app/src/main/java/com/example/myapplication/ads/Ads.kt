package com.example.myapplication.ads

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.libads.core.AdManager
import com.libads.core.AdType
import com.libads.core.AdUnit
import com.libads.core.callback.AdLoadCallback
import com.libads.core.callback.AdResult
import com.libads.core.callback.AdShowCallback
import java.util.concurrent.atomic.AtomicBoolean

object Ads {

    // ════════════════════════════════════════════════════════════════
    //  Preload
    // ════════════════════════════════════════════════════════════════

    fun preload(placement: AdPlacement) {
        Log.i("TAG_ADS", "preload: $placement")
        if (!RemoteAdConfig.isEnableAllAds) return
        val adManager = AdManager.getInstance()

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            val unit2f = applyPlacementConfig(units.unit2f, placement)
            val unitBase = applyPlacementConfig(units.unitBase, placement)

            if (unit2f != null) {
                adManager.preload(unit2f) { result ->
                    if (result !is AdResult.Success && unitBase != null) {
                        adManager.preload(unitBase)
                    }
                }
            } else if (unitBase != null) {
                adManager.preload(unitBase)
            }
        } else {
            val unit = resolveSingle(placement) ?: return
            adManager.preload(applyPlacementConfig(unit, placement)!!)
        }
    }

    /** Preload multiple placements in sequence. */
    fun preload(vararg placements: AdPlacement) {
        placements.forEach { preload(it) }
    }

    // ════════════════════════════════════════════════════════════════
    //  Full-Screen: show (use preloaded cache)
    // ════════════════════════════════════════════════════════════════

    fun show(
        host: FragmentActivity,
        placement: AdPlacement,
        config: AdCallback.() -> Unit = {}
    ) {
        val cb = AdCallback().apply(config)
        val callback = buildShowCallback(cb)

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            AdFullScreenController.showTwoFloor(
                host, units, placement.showLoadingWhenNotReady, callback
            )
        } else {
            val unit = resolveSingle(placement)
            if (unit == null) {
                callback.onAdFailedToShow(-14, "No AdUnit configured for '${placement.name}'")
                return
            }
            AdFullScreenController.show(
                host, unit, placement.showLoadingWhenNotReady, callback
            )
        }
    }

    fun show(
        host: Fragment,
        placement: AdPlacement,
        config: AdCallback.() -> Unit = {}
    ) {
        val cb = AdCallback().apply(config)
        val callback = buildShowCallback(cb)

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            AdFullScreenController.showTwoFloor(
                host, units, placement.showLoadingWhenNotReady, callback
            )
        } else {
            val unit = resolveSingle(placement)
            if (unit == null) {
                callback.onAdFailedToShow(-14, "No AdUnit configured for '${placement.name}'")
                return
            }
            AdFullScreenController.show(
                host, unit, placement.showLoadingWhenNotReady, callback
            )
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Full-Screen: loadAndShow (fresh load, ignores cache)
    // ════════════════════════════════════════════════════════════════

    fun loadAndShow(
        host: FragmentActivity,
        placement: AdPlacement,
        config: AdCallback.() -> Unit = {}
    ) {
        val cb = AdCallback().apply(config)
        val callback = buildShowCallback(cb)

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            AdFullScreenController.loadAndShowTwoFloor(host, units, callback)
        } else {
            val unit = resolveSingle(placement)
            if (unit == null) {
                callback.onAdFailedToShow(-14, "No AdUnit configured for '${placement.name}'")
                return
            }
            AdFullScreenController.loadAndShow(host, unit, callback)
        }
    }

    fun loadAndShow(
        host: Fragment,
        placement: AdPlacement,
        config: AdCallback.() -> Unit = {}
    ) {
        val cb = AdCallback().apply(config)
        val callback = buildShowCallback(cb)

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            AdFullScreenController.loadAndShowTwoFloor(host, units, callback)
        } else {
            val unit = resolveSingle(placement)
            if (unit == null) {
                callback.onAdFailedToShow(-14, "No AdUnit configured for '${placement.name}'")
                return
            }
            AdFullScreenController.loadAndShow(host, unit, callback)
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Inline Ads: showInto (Banner / Native — use preloaded cache)
    // ════════════════════════════════════════════════════════════════

    fun showInto(
        host: FragmentActivity,
        container: ViewGroup,
        placement: AdPlacement,
        onResult: ((AdResult) -> Unit)? = null
    ) {
        if (host.isFinishing || host.isDestroyed ||
            host.lifecycle.currentState == Lifecycle.State.DESTROYED
        ) {
            onResult?.invoke(AdResult.Failure(placement.name, -5, "Activity is finishing or destroyed"))
            return
        }

        when (placement.type) {
            AdType.NATIVE -> showNativeInternal(host, host, container, placement, onResult)
            AdType.BANNER -> showBannerInternal(host, container, placement, onResult)
            else -> error("showInto() only supports BANNER and NATIVE types, got ${placement.type}")
        }
    }

    fun showInto(
        host: Fragment,
        container: ViewGroup,
        placement: AdPlacement,
        onResult: ((AdResult) -> Unit)? = null
    ) {
        val activity = host.activity
        if (activity == null || !host.isAdded || host.isDetached ||
            activity.isFinishing || activity.isDestroyed
        ) {
            onResult?.invoke(AdResult.Failure(placement.name, -5, "Fragment is not attached or destroyed"))
            return
        }
        val lifecycleOwner = host.viewLifecycleOwnerOrFragment()

        when (placement.type) {
            AdType.NATIVE -> showNativeInternal(activity, lifecycleOwner, container, placement, onResult)
            AdType.BANNER -> showBannerInternal(lifecycleOwner, container, placement, onResult)
            else -> error("showInto() only supports BANNER and NATIVE types, got ${placement.type}")
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Inline Ads: loadAndShowInto (fresh load)
    // ════════════════════════════════════════════════════════════════

    fun loadAndShowInto(
        host: FragmentActivity,
        container: ViewGroup,
        placement: AdPlacement,
        onResult: ((AdResult) -> Unit)? = null
    ) {
        destroyPlacement(placement)
        showInto(host, container, placement, onResult)
    }

    fun loadAndShowInto(
        host: Fragment,
        container: ViewGroup,
        placement: AdPlacement,
        onResult: ((AdResult) -> Unit)? = null
    ) {
        destroyPlacement(placement)
        showInto(host, container, placement, onResult)
    }

    // ════════════════════════════════════════════════════════════════
    //  Destroy
    // ════════════════════════════════════════════════════════════════

    fun destroy(placement: AdPlacement) {
        destroyPlacement(placement)
    }

    fun destroy(vararg placements: AdPlacement) {
        placements.forEach { destroyPlacement(it) }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal: Resolve AdUnit(s) from placement
    // ════════════════════════════════════════════════════════════════

    private fun resolveTwo(placement: AdPlacement): TwoFloorAdUnits {
        return AdUnits.getTwoFloor(placement.name, placement.type)
    }

    private fun resolveSingle(placement: AdPlacement): AdUnit? {
        return AdUnits.getUnit(placement.name, placement.type)
    }

    private fun applyPlacementConfig(unit: AdUnit?, placement: AdPlacement): AdUnit? {
        if (unit == null) return null
        return when (placement.type) {
            AdType.BANNER -> unit.copy(collapsiblePositionType = placement.collapsiblePositionType)
            AdType.NATIVE -> unit.copy(nativeLayoutRes = placement.nativeLayoutRes)
            else -> unit
        }
    }

    private fun destroyPlacement(placement: AdPlacement) {
        val adManager = AdManager.getInstance()
        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            units.unit2f?.let { adManager.destroy(it) }
            units.unitBase?.let { adManager.destroy(it) }
        } else {
            resolveSingle(placement)?.let { adManager.destroy(it) }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal: Banner rendering
    // ════════════════════════════════════════════════════════════════

    private fun showBannerInternal(
        lifecycleOwner: LifecycleOwner,
        container: ViewGroup,
        placement: AdPlacement,
        onResult: ((AdResult) -> Unit)?
    ) {
        val handled = AtomicBoolean(false)
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                handled.set(true)
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val adManager = AdManager.getInstance()

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            val unit2f = applyPlacementConfig(units.unit2f, placement)
            val unitBase = applyPlacementConfig(units.unitBase, placement)

            if (unit2f == null && unitBase == null) {
                onResult?.invoke(
                    AdResult.Failure(placement.name, -14, "No AdUnit configured for '${placement.name}'")
                )
                return
            }

            if (unit2f != null) {
                adManager.renderInto(container, unit2f) { result2f ->
                    if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                    when (result2f) {
                        is AdResult.Success -> onResult?.invoke(result2f)
                        is AdResult.Failure, is AdResult.TimedOut -> {
                            if (unitBase != null) {
                                adManager.renderInto(container, unitBase) { baseResult ->
                                    if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                                    onResult?.invoke(baseResult)
                                }
                            } else {
                                onResult?.invoke(result2f)
                            }
                        }
                    }
                }
            } else if (unitBase != null) {
                adManager.renderInto(container, unitBase) { result ->
                    if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                    onResult?.invoke(result)
                }
            }
        } else {
            val unit = applyPlacementConfig(resolveSingle(placement), placement)
            if (unit == null) {
                onResult?.invoke(
                    AdResult.Failure(placement.name, -14, "No AdUnit configured for '${placement.name}'")
                )
                return
            }
            adManager.renderInto(container, unit) { result ->
                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                onResult?.invoke(result)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal: Native rendering
    // ════════════════════════════════════════════════════════════════

    private fun showNativeInternal(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        container: ViewGroup,
        placement: AdPlacement,
        onResult: ((AdResult) -> Unit)?
    ) {
        container.removeAllViews()
        val loadingView = LayoutInflater.from(context)
            .inflate(placement.loadingLayoutRes, container, false)
        container.addView(loadingView)

        val handled = AtomicBoolean(false)
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                handled.set(true)
                owner.lifecycle.removeObserver(this)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val adManager = AdManager.getInstance()

        if (placement.useTwoFloor) {
            val units = resolveTwo(placement)
            val unit2f = applyPlacementConfig(units.unit2f, placement)
            val unitBase = applyPlacementConfig(units.unitBase, placement)

            if (unit2f == null && unitBase == null) {
                container.removeAllViews()
                onResult?.invoke(
                    AdResult.Failure(placement.name, -14, "No native AdUnit configured for '${placement.name}'")
                )
                return
            }

            if (unit2f != null) {
                adManager.renderInto(container, unit2f) { result2f ->
                    if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                    when (result2f) {
                        is AdResult.Success -> onResult?.invoke(result2f)
                        is AdResult.Failure, is AdResult.TimedOut -> {
                            if (unitBase != null) {
                                adManager.renderInto(container, unitBase) { baseResult ->
                                    if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                                    handleNativeResult(container, baseResult, onResult)
                                }
                            } else {
                                handleNativeResult(container, result2f, onResult)
                            }
                        }
                    }
                }
            } else if (unitBase != null) {
                adManager.renderInto(container, unitBase) { result ->
                    if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                    handleNativeResult(container, result, onResult)
                }
            }
        } else {
            val unit = applyPlacementConfig(resolveSingle(placement), placement)
            if (unit == null) {
                container.removeAllViews()
                onResult?.invoke(
                    AdResult.Failure(placement.name, -14, "No native AdUnit configured for '${placement.name}'")
                )
                return
            }
            adManager.renderInto(container, unit) { result ->
                if (handled.get() || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return@renderInto
                handleNativeResult(container, result, onResult)
            }
        }
    }

    private fun handleNativeResult(
        container: ViewGroup,
        result: AdResult,
        onResult: ((AdResult) -> Unit)?
    ) {
        when (result) {
            is AdResult.Success -> onResult?.invoke(result)
            is AdResult.Failure -> {
                container.removeAllViews()
                onResult?.invoke(result)
            }
            is AdResult.TimedOut -> {
                container.removeAllViews()
                onResult?.invoke(result)
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal: Build AdShowCallback from AdCallback DSL
    // ════════════════════════════════════════════════════════════════

    private fun buildShowCallback(cb: AdCallback): AdShowCallback {
        val executed = AtomicBoolean(false)
        val handleNext = {
            if (executed.compareAndSet(false, true)) {
                cb.action?.invoke()
            }
        }
        return object : AdShowCallback {
            override fun onUserEarnedReward(amount: Int, type: String) {
                cb.onRewardEarned?.invoke(amount, type)
            }

            override fun onAdDismissed() {
                cb.onDismissed?.invoke()
                handleNext()
            }

            override fun onAdFailedToShow(errorCode: Int, message: String) {
                cb.onFailed?.invoke(message)
                handleNext()
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal: Fragment lifecycle helper
    // ════════════════════════════════════════════════════════════════

    private fun Fragment.viewLifecycleOwnerOrFragment(): LifecycleOwner {
        return runCatching { viewLifecycleOwner }.getOrDefault(this)
    }
}
