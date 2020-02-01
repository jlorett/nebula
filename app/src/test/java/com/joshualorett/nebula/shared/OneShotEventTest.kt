package com.joshualorett.nebula.shared

import org.junit.Assert.*
import org.junit.Test

/**
 * Test [OneShotEvent].
 * Created by Joshua on 1/19/2020.
 */
class OneShotEventTest {
    @Test
    fun `getHasBeenHandled`() {
        val event = OneShotEvent(Unit)
        event.getContentIfNotHandled()
        assertTrue(event.hasBeenHandled)
    }

    @Test
    fun `getContentIfNotHandled`() {
        val event = OneShotEvent("Test")
        assertEquals("Test", event.getContentIfNotHandled())
    }

    @Test
    fun `doesn't getContentIfNotHandled`() {
        val event = OneShotEvent("Test")
        event.getContentIfNotHandled()
        assertNull(event.getContentIfNotHandled())
    }

    @Test
    fun `peekContent`() {
        val event = OneShotEvent("Test")
        event.getContentIfNotHandled()
        assertEquals("Test", event.peekContent())
    }
}