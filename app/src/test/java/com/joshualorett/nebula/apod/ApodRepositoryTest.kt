package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import retrofit2.Response
import java.time.LocalDate

/**
 * Test [ApodRepository].
 * Created by Joshua on 1/8/2020.
 */
@ExperimentalCoroutinesApi
class ApodRepositoryTest {
    private lateinit var repository: ApodRepository
    private val apodDataSource = mock(ApodDataSource::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val testDate = LocalDate.of(2000, 1, 1)
    private val mockApodResponse = ApodResponse(
        0, "2000-01-01", "apod", "testing",
        "jpg", "v1", "https://example.com",
        "https://example.com/hd"
    )

    @Before
    fun setUp() {
        repository = ApodRepository(apodDataSource, mockApodDao)
    }

    @Test
    fun `returns successful resource`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getApod(testDate)
        assertEquals(Resource.Success(mockApodResponse.toApod()), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `returns unsuccessful resource on network error`() = runBlockingTest {
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodDataSource.getApod(testDate)).thenReturn(errorResponse)
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns error if date too early`() = runBlockingTest {
        val resource = repository.getApod(LocalDate.of(1995, 1, 15))
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns error if cache fails to load`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(null)
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `caches apod from network`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        repository.getApod(testDate)
        verify(mockApodDao).insertApod(mockApodResponse.toApod().toEntity())
    }

    @Test
    fun `clears database before caching`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        repository.getApod(testDate)
        verify(mockApodDao).deleteAll()
    }
}