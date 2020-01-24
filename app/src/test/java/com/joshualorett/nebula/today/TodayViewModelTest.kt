package com.joshualorett.nebula.today

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.joshualorett.nebula.TestCoroutineRule
import com.joshualorett.nebula.TestData
import com.joshualorett.nebula.apod.*
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response
import java.time.LocalDate

/**
 * Test [TodayViewModel].
 * Created by Joshua on 1/12/2020.
 */
@ExperimentalCoroutinesApi
class TodayViewModelTest {
    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    val coroutineRule = TestCoroutineRule()

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    val test = InstantTaskExecutorRule()

    private lateinit var viewModel: TodayViewModel
    private val mockDataSource = mock(ApodDataSource::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val testDate = LocalDate.of(2000, 1, 1)
    private val mockApodResponse = TestData.apodResponse

    @Test
    fun `success state hit`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertEquals(mockApodResponse.toApod(), viewModel.apod.value)
    }

    @Test
    fun `success state turns off loading`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertTrue(viewModel.loading.value == false)
    }

    @Test
    fun `error state hit`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertEquals(viewModel.error.value, "Error getting apod with status 500.")
    }

    @Test
    fun `error state turns off loading`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertTrue(viewModel.loading.value == false)
    }

    @Test
    fun `observe when video links clicked`() = coroutineRule.dispatcher.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "video",
            "v1", "https://example.com",null)
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(videoApod.toApod().toEntity())
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        viewModel.videoLinkClicked()
        assertEquals("https://example.com", viewModel.navigateVideoLink.value?.peekContent())
    }

    @Test
    fun `don't observe when non-video links clicked`() = coroutineRule.dispatcher.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "image",
            "v1", "https://example.com","https://example.com/hd")
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(videoApod.toApod().toEntity())
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        viewModel.videoLinkClicked()
        assertNull(viewModel.navigateVideoLink.value?.peekContent())
    }
}