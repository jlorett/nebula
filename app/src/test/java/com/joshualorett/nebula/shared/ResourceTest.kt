package com.joshualorett.nebula.shared

import org.junit.Assert.*
import org.junit.Test

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

    @Test
    fun `returns data`() {
        val resource = Resource.Success(3)
        assertEquals(3, resource.data)
    }

    @Test
    fun `error null on success`() {
        val resource = Resource.Success(3)
        assertNull(resource.error)
    }

    @Test
    fun `returns error`() {
        val exception = IllegalStateException()
        val resource = Resource.Error(exception)
        assertEquals(exception, resource.error)
    }

    @Test
    fun `data null on error`() {
        val exception = IllegalStateException()
        val resource = Resource.Error(exception)
        assertNull(resource.data)
    }
}
