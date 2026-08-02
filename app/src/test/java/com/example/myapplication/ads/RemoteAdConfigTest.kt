package com.example.myapplication.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RemoteAdConfigTest {
    @Test
    fun `normalization removes invalid duplicate fallback ids and caps two floor`() {
        val config = RemoteAdConfig(
            id = " normal ",
            listId2f = listOf(" 2f ", "2f", "normal", "", "3f", "4f", "5f")
        ).normalized()

        assertEquals("normal", config.id)
        assertEquals(listOf("2f", "3f", "4f"), config.listId2f)
    }

    @Test
    fun `enabled placement with blank id is invalid`() {
        assertFalse(RemoteAdConfig(enabled = true, id = " ").isValid())
    }
}
