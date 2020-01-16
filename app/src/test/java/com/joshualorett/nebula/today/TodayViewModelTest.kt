package com.joshualorett.nebula.today

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.joshualorett.nebula.TestCoroutineRule
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodDataSource
import com.joshualorett.nebula.apod.ApodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Response
import java.time.LocalDate
import java.util.*

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
    private val testDate = LocalDate.of(2000, 1, 1)

    @Test
    fun `loading state hit`() = coroutineRule.dispatcher.runBlockingTest {
        val mockApod = Apod("2000-01-01", "apod", "testing",
        "jpg", "v1", "https://example.com",
        "https://example.com/hd")
        val mockDataSource = mock(ApodDataSource::class.java)
        `when`(mockDataSource.getApod(testDate)).thenReturn(flowOf(Response.success(mockApod)))
        `when`(mockDataSource.getApod(testDate)).thenReturn(flow {
            delay(10)
            Response.success(mockApod)
        })
        val apodRepo = ApodRepository(mockDataSource)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertTrue(viewModel.loading.value == true)
    }

    @Test
    fun `success state hit`() = coroutineRule.dispatcher.runBlockingTest {
        val mockApod = Apod("2000-01-01", "apod", "testing",
            "jpg", "v1", "https://example.com",
            "https://example.com/hd")
        val mockDataSource = mock(ApodDataSource::class.java)
        `when`(mockDataSource.getApod(testDate)).thenReturn(flowOf(Response.success(mockApod)))
        val apodRepo = ApodRepository(mockDataSource)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertEquals(mockApod, viewModel.apod.value)
    }

    @Test
    fun `success state turns off loading`() = coroutineRule.dispatcher.runBlockingTest {
        val mockApod = Apod("2000-01-01", "apod", "testing",
            "jpg", "v1", "https://example.com",
            "https://example.com/hd")
        val mockDataSource = mock(ApodDataSource::class.java)
        `when`(mockDataSource.getApod(testDate)).thenReturn(flowOf(Response.success(mockApod)))
        val apodRepo = ApodRepository(mockDataSource)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertTrue(viewModel.loading.value == false)
    }

    @Test
    fun `error state hit`() = coroutineRule.dispatcher.runBlockingTest {
        val mockDataSource = mock(ApodDataSource::class.java)
        `when`(mockDataSource.getApod(testDate)).thenReturn(flowOf(Response.error(500, "Error".toResponseBody())))
        val apodRepo = ApodRepository(mockDataSource)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertEquals(viewModel.error.value, "Error getting apod with status 500.")
    }

    @Test
    fun `error state turns off loading`() = coroutineRule.dispatcher.runBlockingTest {
        val mockDataSource = mock(ApodDataSource::class.java)
        `when`(mockDataSource.getApod(testDate)).thenReturn(flowOf(Response.error(500, "Error".toResponseBody())))
        val apodRepo = ApodRepository(mockDataSource)
        viewModel = TodayViewModel.TodayViewModelFactory(apodRepo, testDate).create(TodayViewModel::class.java)
        assertTrue(viewModel.loading.value == false)
    }
}