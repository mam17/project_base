package com.libads.core.internal

import com.libads.core.AdType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullScreenAdGateTest {
    private val first = PlacementKey("first", AdType.INTERSTITIAL, "fake")
    private val second = PlacementKey("second", AdType.REWARDED, "fake")

    @Test
    fun `only one placement owns the full screen gate`() {
        val gate = FullScreenAdGate()

        assertTrue(gate.acquire(first))
        assertFalse(gate.acquire(second))
        gate.release(first)
        assertTrue(gate.acquire(second))
    }

    @Test
    fun `wrong placement cannot release the owner`() {
        val gate = FullScreenAdGate()

        assertTrue(gate.acquire(first))
        gate.release(second)
        assertTrue(gate.isShowing())
    }
}
