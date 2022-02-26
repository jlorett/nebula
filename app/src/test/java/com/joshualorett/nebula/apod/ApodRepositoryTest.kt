package com.joshualorett.nebula.apod

import android.content.Context
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.testing.TestData
import com.joshualorett.nebula.testing.mainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
    private val apodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val testDate = LocalDate.of(2000, 1, 1)
    private val mockApodResponse = TestData.apodResponse

    @Before
    fun setUp() {
        repository = ApodRepository(
            apodService,
            mockApodDao,
            mockImageCache
        )
    }

    @Test
    fun `returns successful resource`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getApod(testDate)
        assertEquals(Resource.Success(mockApodResponse.toApod()), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `returns error on null data`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(null))
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns unsuccessful resource on network error`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodService.getApod(testDate.toString())).thenReturn(errorResponse)
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns unsuccessful resource on exception`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        given(apodService.getApod(testDate.toString())).willAnswer {
            throw IOException()
        }
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns error if date too early`() = mainCoroutineRule.runBlockingTest {
        val resource = repository.getApod(LocalDate.of(1995, 1, 15))
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `returns error if cache fails to load`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(null)
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `caches apod from network`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        repository.getApod(testDate)
        verify(mockApodDao).insertApod(mockApodResponse.toApod().toEntity())
    }

    @Test
    fun `clears database before caching`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        repository.getApod(testDate)
        verify(mockApodDao).delete(mockApodResponse.toApod().toEntity())
    }

    @Test
    fun `clears cache`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        mockImageCache.attachApplicationContext(mock(Context::class.java))
        repository.getApod(testDate)
        repository.clearCache()
        mockImageCache.detachApplicationContext()
        verify(mockImageCache).clear()
    }

    @Test
    fun `gets apod from database by id`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        val cachedApod = repository.getCachedApod(mockApodResponse.id) as Resource.Success<Apod>
        assertEquals(mockApodResponse.toApod(), cachedApod.data)
    }

    @Test
    fun `errors getting apod from database by id`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(null)
        val resource = repository.getCachedApod(mockApodResponse.id)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `gets cached apod by date`() = mainCoroutineRule.runBlockingTest {
        val entity = TestData.apodEntity
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(entity)
        val resource = repository.getApod(testDate) as Resource.Success<Apod>
        assertEquals(resource.data, entity.toApod())
    }

    @Test
    fun `fresh returns successful resource`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        val resource = repository.getFreshApod(testDate)
        assertEquals(Resource.Success(mockApodResponse.toApod()), resource)
        assertTrue(resource is Resource.Success)
    }

    @Test
    fun `fresh returns error if date too early`() = mainCoroutineRule.runBlockingTest {
        val resource = repository.getFreshApod(LocalDate.of(1995, 1, 15))
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns error on null data`() = mainCoroutineRule.runBlockingTest {
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(null))
        val resource = repository.getFreshApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns unsuccessful resource on network error`() = mainCoroutineRule.runBlockingTest {
        val errorResponse: Response<ApodResponse> = Response.error(500, "Error".toResponseBody())
        `when`(apodService.getApod(testDate.toString())).thenReturn(errorResponse)
        val resource = repository.getFreshApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh returns unsuccessful resource on exception`() = mainCoroutineRule.runBlockingTest {
        given(apodService.getApod(testDate.toString())).willAnswer {
            throw IOException()
        }
        val resource = repository.getFreshApod(testDate)
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun `fresh clears database before caching`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(apodService.getApod(testDate.toString())).thenReturn(Response.success(mockApodResponse))
        repository.getFreshApod(testDate)
        verify(mockApodDao).delete(mockApodResponse.toApod().toEntity())
    }
}
