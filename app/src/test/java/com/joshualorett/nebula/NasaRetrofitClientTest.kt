package com.joshualorett.nebula

import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.api.NasaRetrofitClient
import org.junit.Assert.*
import org.junit.Test

/**
 * Test [NasaRetrofitClient].
 * Created by Joshua on 1/7/2020.
 */
class NasaRetrofitClientTest {
    @Test
    fun `creates service class`() {
        val service = NasaRetrofitClient.create("testKey", ApodService::class.java)
        assertNotNull(service)
    }
}