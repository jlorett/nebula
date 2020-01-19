package com.joshualorett.nebula.apod.api

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
import java.time.LocalDate

/**
 * Test [ApodRemoteDataSource].
 * Created by Joshua on 1/5/2020.
 */
@ExperimentalCoroutinesApi
class ApodRemoteDataSourceTest {
    private val testKey = "testKey"
    private val testDate = LocalDate.of(2000, 1, 1)
    private val mockRetrofitServiceDelegate = mock(RetrofitServiceDelegate::class.java)
    private val mockService = mock(ApodService::class.java)
    private lateinit var apodDataSource: ApodDataSource

    @Before
    fun setup() {
        `when`(mockRetrofitServiceDelegate.create(ApodService::class.java)).thenReturn(mockService)
        apodDataSource = ApodRemoteDataSource(
            mockRetrofitServiceDelegate,
            testKey
        )
    }

    @Test
    fun `api returns successful response`() = runBlockingTest {
        val mockApodResponse = ApodResponse(
            0, "2000-01-01", "apod", "testing",
            "jpg", "v1", "https://example.com",
            "https://example.com/hd"
        )
        val success: Response<ApodResponse> = Response.success(mockApodResponse)
        `when`(mockService.getApod(anyString(), anyString())).thenReturn(success)
        val result = apodDataSource.getApod(testDate)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `api error returns unsuccessful response`() = runBlockingTest {
        val error: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(mockService.getApod(anyString(), anyString())).thenReturn(error)
        val result = apodDataSource.getApod(testDate)
        assertFalse(result.isSuccessful)
    }
}