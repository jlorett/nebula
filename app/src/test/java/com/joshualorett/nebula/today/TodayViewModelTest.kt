package com.joshualorett.nebula.today

import androidx.lifecycle.SavedStateHandle
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.apod.toEntity
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import com.joshualorett.nebula.testing.MainCoroutineRule
import com.joshualorett.nebula.testing.TestData
import com.joshualorett.nebula.ui.today.TodayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import retrofit2.Response
import java.time.LocalDate

/**
 * Test [TodayViewModel].
 * Created by Joshua on 1/12/2020.
 */
@ExperimentalCoroutinesApi
class TodayViewModelTest {
    private lateinit var viewModel: TodayViewModel
    private val mockApodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val today = LocalDate.now()
    private val mockApodResponse = TestData.apodResponse

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `factory creates ViewModel`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(anyString())).thenReturn(mockApodResponse.toApod().toEntity())
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, Dispatchers.Main)
        assertNotNull(TodayViewModel(apodRepo, SavedStateHandle()))
    }

    @Test
    fun `initial load uses today's date`() = mainCoroutineRule.runBlockingTest {
        val todayResponse = ApodResponse(
            0, today.toString(), "apod", "testing",
            "image", "v1", "https://example.com",
            "https://example.com/hd"
        )
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(todayResponse.toApod().toEntity())
        `when`(mockApodDao.loadById(anyLong())).thenReturn(todayResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(todayResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(todayResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        val job = launch {
            assertEquals(todayResponse.toApod(), viewModel.apod.last().data)
        }
        job.cancel()
    }

    @Test
    fun `success state hit`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        val job = launch {
            assertEquals(mockApodResponse.toApod(), viewModel.apod.first().data)
        }
        job.cancel()
    }

    @Test
    fun `error state hit`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        val job = launch {
            assertEquals("Error getting apod with status 500.", viewModel.apod.first().error)
        }
        job.cancel()
    }

    @Test
    fun `observe when video links clicked`() = mainCoroutineRule.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "video",
            "v1", "https://example.com", null
        )
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(videoApod.toApod().toEntity())
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.first()
        var url: String? = null
        launch {
            url = viewModel.navigateVideoLink.conflate().first()
        }
        viewModel.videoLinkClicked()
        assertEquals("https://example.com", url)
    }

    @Test
    fun `don't observe when non-video links clicked`() = mainCoroutineRule.runBlockingTest {
        val imageApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "image",
            "v1", "https://example.com", "https://example.com/hd"
        )
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(imageApod.toApod().toEntity())
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(imageApod))
        `when`(mockApodDao.insertApod(imageApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.first()
        var url: String? = null
        val job = launch {
            url = viewModel.navigateVideoLink.conflate().firstOrNull()
        }
        viewModel.videoLinkClicked()
        job.cancel()
        assertNull(url)
    }

    @Test
    fun `observe photo clicked`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.first()
        var id = 0L
        launch {
            id = viewModel.navigateFullPicture.conflate().first()
        }
        viewModel.onPhotoClicked()
        assertEquals(mockApodResponse.id, id)
    }

    @Test
    fun `don't observe photo clicked on null apods`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
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
    fun `refresh updates data`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        val refreshedApodResponse = ApodResponse(
            0, "2000-02-01", "apodRefreshed", "testingRefresh",
            "image", "v1", "https://exampleRefresh.com",
            "https://exampleRefresh.com/hd"
        )
        `when`(mockApodDao.loadById(anyLong())).thenReturn(refreshedApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(refreshedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(refreshedApodResponse))
        viewModel.refresh()
        assertEquals(refreshedApodResponse.toApod(), viewModel.apod.first().data)
    }

    @Test
    fun `update date hit`() = mainCoroutineRule.runBlockingTest {
        val testDate = LocalDate.of(2000, 2, 1)
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        val updatedApodResponse = ApodResponse(
            0, "2000-02-01", "apodUpdate", "testingUpdate",
            "image", "v1", "https://exampleUpdated.com",
            "https://exampleUpdated.com/hd"
        )
        `when`(mockApodDao.loadByDate(testDate.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(updatedApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(updatedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockApodService.getApod(testDate.toString())).thenReturn(Response.success(updatedApodResponse))
        viewModel.updateDate(testDate)
        assertEquals(updatedApodResponse.toApod(), viewModel.apod.first().data)
    }

    @Test
    fun `refresh clears error`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.first()
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        viewModel.refresh()
        assertNull(viewModel.apod.first().error)
    }

    @Test
    fun `update clears error`() = mainCoroutineRule.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, SavedStateHandle())
        viewModel.apod.first()
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodService.getApod(today.toString())).thenReturn(Response.success(mockApodResponse))
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        viewModel.updateDate(today)
        assertNull(viewModel.apod.first().error)
    }

    @Test
    fun `date picker updates with today if current date null`() = mainCoroutineRule.runBlockingTest {
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
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
    fun `uses date in SaveStateHandle`() = mainCoroutineRule.runBlockingTest {
        val testDate = LocalDate.parse("2000-01-01")
        val state = SavedStateHandle(mapOf("date" to testDate))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, mainCoroutineRule.dispatcher)
        viewModel = TodayViewModel(apodRepo, state)
        var date: LocalDate? = null
        launch {
            date = viewModel.showDatePicker.conflate().first()
        }
        viewModel.onChooseDate()
        assertEquals(testDate, date)
    }
}
