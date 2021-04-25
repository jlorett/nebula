package com.joshualorett.nebula.apod

import android.content.Context
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.testing.TestData
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import retrofit2.Response

/**
 * Test [ApodRepository].
 * Created by Joshua on 1/8/2020.
 */
@ExperimentalCoroutinesApi
class ApodRepositoryTest {
    private lateinit var repository: ApodRepository
    private val apodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val testDate = LocalDate.of(2000, 1, 1)
    private val mockApodResponse = TestData.apodResponse
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        repository = ApodRepository(apodService, mockApodDao, mockImageCache, testDispatcher)
    }

    @Test
    fun `returns successful resource`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getApod(testDate).first()
        assertEquals(Resource.Success(mockApodResponse.toApod()), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `returns error on null data`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(null))
        val resource = repository.getApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns unsuccessful resource on network error`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodService.getApod(testDate.toString())).thenReturn(errorResponse)
        val resource = repository.getApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns unsuccessful resource on exception`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        given(apodService.getApod(testDate.toString())).willAnswer {
            flow<Unit> {
                throw IOException()
            }
        }
        val resource = repository.getApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns error if date too early`() = testDispatcher.runBlockingTest {
        val resource = repository.getApod(LocalDate.of(1995, 1, 15)).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns error if cache fails to load`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(null))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `caches apod from network`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        repository.getApod(testDate).first()
        verify(mockApodDao).insertApod(mockApodResponse.toApod().toEntity())
    }

    @Test
    fun `clears database before caching`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        repository.getApod(testDate).first()
        verify(mockApodDao).delete(mockApodResponse.toApod().toEntity())
    }

    @Test
    fun `clears cache`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        mockImageCache.attachApplicationContext(mock(Context::class.java))
        repository.getApod(testDate)
        repository.clearCache()
        mockImageCache.detachApplicationContext()
        verify(mockImageCache).clear()
    }

    @Test
    fun `gets apod from database by id`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        val cachedApod = repository.getApod(mockApodResponse.id).first() as Resource.Success<Apod>
        assertEquals(mockApodResponse.toApod(), cachedApod.data)
    }

    @Test
    fun `errors getting apod from database by id`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(null))
        val resource = repository.getApod(mockApodResponse.id).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `gets cached apod by date`() = testDispatcher.runBlockingTest {
        val entity = TestData.apodEntity
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(entity))
        val resource = repository.getApod(testDate).first() as Resource.Success<Apod>
        assertEquals(resource.data, entity.toApod())
    }

    @Test
    fun `fresh returns successful resource`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getFreshApod(testDate).first()
        assertEquals(Resource.Success(mockApodResponse.toApod()), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `fresh returns error if date too early`() = testDispatcher.runBlockingTest {
        val resource = repository.getFreshApod(LocalDate.of(1995, 1, 15)).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns error on null data`() = testDispatcher.runBlockingTest {
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(null))
        val resource = repository.getFreshApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns unsuccessful resource on network error`() = testDispatcher.runBlockingTest {
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodService.getApod(testDate.toString())).thenReturn(errorResponse)
        val resource = repository.getFreshApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns unsuccessful resource on exception`() = testDispatcher.runBlockingTest {
        given(apodService.getApod(testDate.toString())).willAnswer {
            flow<Unit> {
                throw IOException()
            }
        }
        val resource = repository.getFreshApod(testDate).first()
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh clears database before caching`() = testDispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        repository.getFreshApod(testDate).first()
        verify(mockApodDao).delete(mockApodResponse.toApod().toEntity())
    }
}
