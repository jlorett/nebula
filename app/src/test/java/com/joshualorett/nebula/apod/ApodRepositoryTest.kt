package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Result
import com.joshualorett.nebula.shared.Status.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response
import java.util.*

/**
 * Test [ApodRepository].
 * Created by Joshua on 1/8/2020.
 */
@ExperimentalCoroutinesApi
class ApodRepositoryTest {
    private lateinit var repository: ApodRepository
    private val apodDataSource = mock(ApodDataSource::class.java)
    private val testDate = Date(2000, 0, 1)

    @Before
    fun setUp() {
        repository = ApodRepository(apodDataSource)
    }

    @Test
    fun `returns successful result`() = runBlockingTest {
        val mockApod = Apod("2000-01-01", "apod", "testing",
            "jpg", "v1", "https://example.com",
            "https://example.com/hd")
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApod))
        val result = repository.getApod(testDate)
        assertEquals(Result.success(mockApod), result)
        assertTrue(result.status == Success)
    }

    @Test
    fun `returns unsuccessful result`() = runBlockingTest {
        val errorResponse: Response<Apod> = Response.error(500, "Error".toResponseBody())
        `when`(apodDataSource.getApod(testDate)).thenReturn(errorResponse)
        val result = repository.getApod(testDate)
        assertTrue(result.status == Error)
    }
}