package com.joshualorett.nebula.apod

import android.content.Context
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.TestData
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import retrofit2.Response
import java.io.IOException
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
    private val mockImageCache = mock(ImageCache::class.java)
    private val testDate = LocalDate.of(2000, 1, 1)
    private val mockApodResponse = TestData.apodResponse

    @Before
    fun setUp() {
        repository = ApodRepository(apodDataSource, mockApodDao, mockImageCache)
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
    fun `returns error on null data`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(null))
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns unsuccessful resource on network error`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodDataSource.getApod(testDate)).thenReturn(errorResponse)
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns unsuccessful resource on exception`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        given(apodDataSource.getApod(testDate)).willAnswer {
            throw IOException()
        }
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

    @Test
    fun `clears cache`() = runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        mockImageCache.attachApplicationContext(mock(Context::class.java))
        repository.getApod(testDate)
        mockImageCache.detachApplicationContext()
        verify(mockImageCache).clear()
    }

    @Test
    fun `gets apod from database by id`() = runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        val cachedApod = repository.getCachedApod(mockApodResponse.id) as Resource.Success<Apod>
        assertEquals(mockApodResponse.toApod(), cachedApod.data)
    }

    @Test
    fun `errors getting apod from database by id`() = runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(null)
        val resource = repository.getCachedApod(mockApodResponse.id)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `gets cached apod by date`() = runBlockingTest {
        val entity = TestData.apodEntity
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(entity)
        val resource = repository.getApod(testDate) as Resource.Success<Apod>
        assertEquals(resource.data, entity.toApod())
    }

    @Test
    fun `fresh returns successful resource`() = runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getFreshApod(testDate)
        assertEquals(Resource.Success(mockApodResponse.toApod()), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `fresh returns error if date too early`() = runBlockingTest {
        val resource = repository.getFreshApod(LocalDate.of(1995, 1, 15))
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns error on null data`() = runBlockingTest {
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(null))
        val resource = repository.getFreshApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns unsuccessful resource on network error`() = runBlockingTest {
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodDataSource.getApod(testDate)).thenReturn(errorResponse)
        val resource = repository.getFreshApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns unsuccessful resource on exception`() = runBlockingTest {
        given(apodDataSource.getApod(testDate)).willAnswer {
            throw IOException()
        }
        val resource = repository.getFreshApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh clears database before caching`() = runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        repository.getFreshApod(testDate)
        verify(mockApodDao).deleteAll()
    }
}