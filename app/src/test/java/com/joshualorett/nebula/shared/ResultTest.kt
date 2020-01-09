package com.joshualorett.nebula.shared

import org.junit.Test

import org.junit.Assert.*

/**
 * Test [Result].
 * Created by Joshua on 1/8/2020.
 */
class ResultTest {
    @Test
    fun `returns loading`() {
        assertEquals(Result.loading().status, Status.Loading)
    }

    @Test
    fun `returns error`() {
        assertEquals(Result.error("", null).status, Status.Error)
    }

    @Test
    fun `returns success`() {
        assertEquals(Result.success(1).status, Status.Success)
    }
}