package com.joshualorett.nebula.today

import androidx.lifecycle.SavedStateHandle
import com.joshualorett.nebula.TestData
import com.joshualorett.nebula.ViewModelTest
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.apod.toEntity
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import retrofit2.Response
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Test [TodayViewModel].
 * Created by Joshua on 1/12/2020.
 */
@ExperimentalCoroutinesApi
class TodayViewModelTest: ViewModelTest() {
    private lateinit var viewModel: TodayViewModel
    private val mockApodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val today = LocalDate.now()
    private val mockApodResponse = TestData.apodResponse
    
    @Test
    fun `factory creates ViewModel`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(anyString())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        assertNotNull(TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle()))
    }

    @Test
    fun `initial load uses today's date`() = coroutineRule.runBlockingTest {
        val todayResponse = ApodResponse(
            0, today.toString(), "apod", "testing",
            "image", "v1", "https://example.com",
            "https://example.com/hd"
        )
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(todayResponse.toApod().toEntity()))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(todayResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(todayResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(todayResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.apod.getOrAwaitValue()
        assertEquals(todayResponse.toApod(), viewModel.apod.getOrAwaitValue().data)
    }

    @Test
    fun `success state hit`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        assertEquals(mockApodResponse.toApod(), viewModel.apod.getOrAwaitValue(1).data)
    }

    @Test
    fun `error state hit`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        assertEquals(viewModel.apod.getOrAwaitValue(1).error, "Error getting apod with status 500.")
    }

    @Test
    fun `observe when video links clicked`() = coroutineRule.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "video",
            "v1", "https://example.com",null)
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(videoApod.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.apod.getOrAwaitValue(1)
        viewModel.videoLinkClicked()
        assertEquals("https://example.com", viewModel.navigateVideoLink.getOrAwaitValue().peekContent())
    }

    @Test(expected = TimeoutException::class)
    fun `don't observe when non-video links clicked`() = coroutineRule.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "image",
            "v1", "https://example.com","https://example.com/hd")
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(videoApod.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.apod.getOrAwaitValue(1)
        viewModel.videoLinkClicked()
        viewModel.navigateVideoLink.getOrAwaitValue(time = 300, timeUnit = TimeUnit.MILLISECONDS)
        fail("NavigateVideoLink shouldn't have been called.")
    }

    @Test
    fun `observe photo clicked`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.apod.getOrAwaitValue(1)
        viewModel.onPhotoClicked()
        assertEquals(viewModel.navigateFullPicture.getOrAwaitValue().peekContent(), mockApodResponse.id)
    }

    @Test(expected = TimeoutException::class)
    fun `don't observe photo clicked on null apods`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.onPhotoClicked()
        viewModel.navigateFullPicture.getOrAwaitValue(time = 300, timeUnit = TimeUnit.MILLISECONDS)
        fail("NavigateFullPicture shouldn't have been called.")
    }

    @Test
    fun `refresh updates data`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        val refreshedApodResponse = ApodResponse(
            0, "2000-02-01", "apodRefreshed", "testingRefresh",
            "image", "v1", "https://exampleRefresh.com",
            "https://exampleRefresh.com/hd"
        )
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(refreshedApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(refreshedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(refreshedApodResponse))
        viewModel.refresh()
        viewModel.apod.getOrAwaitValue()
        assertEquals(refreshedApodResponse.toApod(), viewModel.apod.getOrAwaitValue().data)
    }

    @Test
    fun `update date hit`() = coroutineRule.runBlockingTest {
        val testDate = LocalDate.of(2000, 2, 1)
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        val updatedApodResponse = ApodResponse(
            0, "2000-02-01", "apodUpdate", "testingUpdate",
            "image", "v1", "https://exampleUpdated.com",
            "https://exampleUpdated.com/hd"
        )
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(updatedApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(updatedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockApodService.getApod(testDate.toString())).thenReturn(Response.success(updatedApodResponse))
        viewModel.updateDate(testDate)
        assertEquals(updatedApodResponse.toApod(), viewModel.apod.getOrAwaitValue(1).data)
    }

    @Test
    fun `refresh clears error`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.apod.getOrAwaitValue()
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        viewModel.refresh()
        assertNull(viewModel.apod.getOrAwaitValue().error)
    }

    @Test
    fun `update clears error`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.apod.getOrAwaitValue()
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        viewModel.updateDate(today)
        assertNull(viewModel.apod.getOrAwaitValue().error)
    }

    @Test
    fun `date picker updates with today if current date null`() = coroutineRule.runBlockingTest {
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, SavedStateHandle())
        viewModel.onChooseDate()
        val event = viewModel.showDatePicker.getOrAwaitValue()
        val expected = LocalDate.now()
        assertEquals(expected, event.peekContent())
    }

    @Test
    fun `uses date in SaveStateHandle`() = coroutineRule.runBlockingTest {
        val testDate = LocalDate.parse("2000-01-01")
        val state = SavedStateHandle(mapOf("date" to testDate))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo, coroutineRule.dispatcher, state)
        viewModel.onChooseDate()
        val event = viewModel.showDatePicker.getOrAwaitValue()
        assertEquals(testDate, event.peekContent())
    }
}