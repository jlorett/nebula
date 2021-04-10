package com.joshualorett.nebula.today

import androidx.lifecycle.*
import com.joshualorett.nebula.testing.TestData
import com.joshualorett.nebula.testing.ViewModelTest
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.apod.toEntity
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import retrofit2.Response
import java.time.LocalDate

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
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        assertNotNull(TodayViewModel(apodRepo, SavedStateHandle()))
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
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        assertEquals(todayResponse.toApod(), viewModel.apod.conflate().first().data)
    }

    @Test
    fun `success state hit`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        assertEquals(mockApodResponse.toApod(), viewModel.apod.conflate().first().data)
    }

    @Test
    fun `error state hit`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        assertEquals("Error getting apod with status 500.", viewModel.apod.conflate().first().error)
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
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.conflate().first()
        var url: String? = null
        launch {
            url = viewModel.navigateVideoLink.conflate().first()
        }
        viewModel.videoLinkClicked()
        assertEquals("https://example.com", url)
    }

    @Test
    fun `don't observe when non-video links clicked`() = coroutineRule.runBlockingTest {
        val imageApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "image",
            "v1", "https://example.com","https://example.com/hd")
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(imageApod.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(imageApod))
        `when`(mockApodDao.insertApod(imageApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.conflate().first()
        var url: String? = null
        val job = launch {
            url = viewModel.navigateVideoLink.conflate().firstOrNull()
        }
        viewModel.videoLinkClicked()
        job.cancel()
        assertNull(url)
    }

    @Test
    fun `observe photo clicked`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.conflate().first()
        var id = 0L
        launch {
            id = viewModel.navigateFullPicture.conflate().first()
        }
        viewModel.onPhotoClicked()
        assertEquals(mockApodResponse.id, id)
    }

    @Test
    fun `don't observe photo clicked on null apods`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        var id = 0L
        val job = launch {
            id = viewModel.navigateFullPicture.firstOrNull() ?: 0L
        }
        viewModel.onPhotoClicked()
        job.cancel()
        assertEquals(0, id)
    }

    @Test
    fun `refresh updates data`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        val refreshedApodResponse = ApodResponse(
            0, "2000-02-01", "apodRefreshed", "testingRefresh",
            "image", "v1", "https://exampleRefresh.com",
            "https://exampleRefresh.com/hd"
        )
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(refreshedApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(refreshedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(refreshedApodResponse))
        viewModel.refresh()
        assertEquals(refreshedApodResponse.toApod(), viewModel.apod.conflate().first().data)
    }

    @Test
    fun `update date hit`() = coroutineRule.runBlockingTest {
        val testDate = LocalDate.of(2000, 2, 1)
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
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
        assertEquals(updatedApodResponse.toApod(), viewModel.apod.conflate().first().data)
    }

    @Test
    fun `refresh clears error`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.conflate().first()
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        viewModel.refresh()
        assertNull(viewModel.apod.conflate().first().error)
    }

    @Test
    fun `update clears error`() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(null))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.conflate().first()
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(flowOf(mockApodResponse.toApod().toEntity()))
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        viewModel.updateDate(today)
        assertNull(viewModel.apod.conflate().first().error)
    }

    @Test
    fun `date picker updates with today if current date null`() = coroutineRule.runBlockingTest {
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        var date: LocalDate? = null
        launch {
            date = viewModel.showDatePicker.conflate().first()
        }
        viewModel.onChooseDate()
        val expected = LocalDate.now()
        assertEquals(expected, date)
    }

    @Test
    fun `uses date in SaveStateHandle`() = coroutineRule.runBlockingTest {
        val testDate = LocalDate.parse("2000-01-01")
        val state = SavedStateHandle(mapOf("date" to testDate))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, state)
        var date: LocalDate? = null
        launch {
            date = viewModel.showDatePicker.conflate().first()
        }
        viewModel.onChooseDate()
        assertEquals(testDate, date)
    }
}