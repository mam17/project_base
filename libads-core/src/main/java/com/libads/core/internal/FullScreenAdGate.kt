package com.libads.core.internal

import java.util.concurrent.atomic.AtomicReference

internal class FullScreenAdGate {
    private val activePlacement = AtomicReference<PlacementKey?>(null)

    fun isShowing(): Boolean = activePlacement.get() != null

    fun acquire(placement: PlacementKey): Boolean {
        return activePlacement.compareAndSet(null, placement)
    }

    fun release(placement: PlacementKey) {
        activePlacement.compareAndSet(placement, null)
    }

    fun clear() {
        activePlacement.set(null)
    }
}
