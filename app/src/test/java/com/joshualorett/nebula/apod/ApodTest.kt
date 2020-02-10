package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Test [Apod].
 * Created by Joshua on 1/19/2020.
 */
class ApodTest {
    @Test
    fun `hdUrl defaults to null`() {
        val apod = Apod(1, "", "", "", "", "")
        assertNull(apod.hdurl)
    }

    @Test
    fun `copyright defaults to null`() {
        val apod = Apod(1, "", "", "", "", "")
        assertNull(apod.copyright)
    }

    @Test
    fun `apodResponse converts to apod`() {
        val apodResponse = ApodResponse(
            0, "2000-01-01", "apod", "testing",
            "image", "v1", "https://example.com",
            "https://example.com/hd"
        )
        val expected = Apod(0, "2000-01-01", "apod", "testing",
            "image", "https://example.com", "https://example.com/hd")
        assertEquals(expected, apodResponse.toApod())
    }

    @Test
    fun `apodEntity converts to apod`() {
        val apodEntity = ApodEntity(1, "2000-01-01", "apod", "testing",
            "image", "https://example.com", "https://example.com/hd", "tester")
        val expected = Apod(1, "2000-01-01", "apod", "testing",
            "image", "https://example.com", "https://example.com/hd", "tester")
        assertEquals(expected, apodEntity.toApod())
    }

    @Test
    fun `apod converts to apodEntity`() {
        val apod = Apod(0, "2000-01-01", "apod", "testing",
            "image", "https://example.com", "https://example.com/hd", "tester")
        val expected = ApodEntity(0, "2000-01-01", "apod", "testing",
            "image", "https://example.com", "https://example.com/hd", "tester")
        assertEquals(expected, apod.toEntity())
    }

    @Test
    fun `apod has image`() {
        val apod = Apod(
            0, "2000-01-01", "apod", "testing",
            "image", "https://example.com", "https://example.com/hd", "tester"
        )
        assertTrue(apod.hasImage())
    }

    @Test
    fun `apod doesn't have image for videos`() {
        val apod = Apod(
            0, "2000-01-01", "apod", "testing",
            "video", "https://example.com", "https://example.com/hd", "tester"
        )
        assertFalse(apod.hasImage())
    }
}