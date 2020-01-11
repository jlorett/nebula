package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
    fun `returns successful resource`() = runBlockingTest {
        val mockApod = Apod("2000-01-01", "apod", "testing",
            "jpg", "v1", "https://example.com",
            "https://example.com/hd")
        `when`(apodDataSource.getApod(testDate)).thenReturn(flowOf(Response.success(mockApod)))
        val resource = repository.getApod(testDate).first()
        assertEquals(Resource.Success(mockApod), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `returns unsuccessful resource`() = runBlockingTest {
        val errorResponse: Response<Apod> = Response.error(500, "Error".toResponseBody())
        `when`(apodDataSource.getApod(testDate)).thenReturn(flowOf(errorResponse))
        val resource = repository.getApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }
}