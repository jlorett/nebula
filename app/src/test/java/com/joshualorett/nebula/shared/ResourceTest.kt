package com.joshualorett.nebula.shared

import org.junit.Test

import org.junit.Assert.*

/**
 * Test [Resource].
 * Created by Joshua on 1/8/2020.
 */
class ResourceTest {
    @Test
    fun `returns loading`() {
        assertEquals(Resource.Loading.status, Status.Loading)
    }

    @Test
    fun `returns error`() {
        assertEquals(Resource.Error("", null).status, Status.Error)
    }

    @Test
    fun `returns success`() {
        assertEquals(Resource.Success(1).status, Status.Success)
    }
}