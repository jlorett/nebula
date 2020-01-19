package com.joshualorett.nebula

import com.joshualorett.nebula.apod.api.ApodService
import org.junit.Test

import org.junit.Assert.*

/**
 * Test [NasaRetrofitClient].
 * Created by Joshua on 1/7/2020.
 */
class NasaRetrofitClientTest {
    @Test
    fun `retrofit instance not null`() {
        assertNotNull(NasaRetrofitClient.retrofit)
    }

    @Test
    fun `creates service class`() {
        val service = NasaRetrofitClient.create(ApodService::class.java)
        assertNotNull(service)
    }

    @Test
    fun `base url matches nasa api url`() {
        val baseUrl = NasaRetrofitClient.retrofit.baseUrl()
        assertEquals("https://api.nasa.gov/", baseUrl.toString())
    }

    @Test
    fun `base url uses https`() {
        val baseUrl = NasaRetrofitClient.retrofit.baseUrl()
        assertTrue(baseUrl.isHttps)
    }
}