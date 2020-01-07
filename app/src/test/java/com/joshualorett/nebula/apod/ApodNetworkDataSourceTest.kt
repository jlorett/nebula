package com.joshualorett.nebula.apod

import com.joshualorett.nebula.RetrofitServiceDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response
import java.util.*

/**
 * Test [ApodNetworkDataSource].
 * Created by Joshua on 1/5/2020.
 */
@ExperimentalCoroutinesApi
class ApodNetworkDataSourceTest {
    private val testKey = "testKey"
    private val testDate = Date(2000, 0, 1)
    private val mockRetrofitServiceDelegate = mock(RetrofitServiceDelegate::class.java)
    private val mockService = mock(ApodService::class.java)
    private lateinit var apodDataSource: ApodDataSource

    @Before
    fun setup() {
        `when`(mockRetrofitServiceDelegate.create(ApodService::class.java)).thenReturn(mockService)
        apodDataSource = ApodNetworkDataSource(mockRetrofitServiceDelegate, testKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throw exception if date is before first apod`() = runBlockingTest {
        apodDataSource.getApod(Date(1995, 0, 15))
    }

    @Test
    fun `api returns successful response`() = runBlockingTest {
        val mockApod = Apod("2000-01-01", "apod", "testing",
            "jpg", "v1", "https://example.com",
            "https://example.com/hd")
        val success: Response<Apod> = Response.success(mockApod)
        `when`(mockService.getApod(anyString(), anyString())).thenReturn(success)
        val result = apodDataSource.getApod(testDate)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `api error returns unsuccessful response`() = runBlockingTest {
        val error: Response<Apod> = Response.error(500, "Error".toResponseBody())
        `when`(mockService.getApod(anyString(), anyString())).thenReturn(error)
        val result = apodDataSource.getApod(testDate)
        assertFalse(result.isSuccessful)
    }
}