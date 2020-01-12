package com.joshualorett.nebula.shared

import org.junit.Test

import org.junit.Assert.*

/**
 * Test [Resource].
 * Created by Joshua on 1/8/2020.
 */
class ResourceTest {
    @Test
    fun `loading not successful`() {
        val resource = Resource.Loading
        assertFalse(resource.successful())
    }

    @Test
    fun `error not successful`() {
        val resource = Resource.Error(null)
        assertFalse(resource.successful())
    }

    @Test
    fun `success is successful`() {
        val resource = Resource.Success(1)
        assertTrue(resource.successful())
    }
}