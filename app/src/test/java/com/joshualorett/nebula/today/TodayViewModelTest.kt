package com.joshualorett.nebula.today

import com.joshualorett.nebula.TestData
import com.joshualorett.nebula.ViewModelTest
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.apod.toEntity
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val mockDataSource = mock(ApodDataSource::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val today = LocalDate.now()
    private val mockApodResponse = TestData.apodResponse

    @Test
    fun `factory creates ViewModel`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(anyString())).thenReturn(mockApodResponse.toApod().toEntity())
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        assertNotNull(TodayViewModel(apodRepo))
    }

    @Test
    fun `initial load uses today's date`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        verify(mockApodDao).loadByDate(today.toString())
    }

    @Test
    fun `success state hit`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        assertEquals(mockApodResponse.toApod(), viewModel.apod.value?.data)
    }

    @Test
    fun `error state hit`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        assertEquals(viewModel.apod.value?.error, "Error getting apod with status 500.")
    }

    @Test
    fun `observe when video links clicked`() = coroutineRule.dispatcher.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "video",
            "v1", "https://example.com",null)
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(videoApod.toApod().toEntity())
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        viewModel.videoLinkClicked()
        assertEquals("https://example.com", viewModel.navigateVideoLink.value?.peekContent())
    }

    @Test
    fun `don't observe when non-video links clicked`() = coroutineRule.dispatcher.runBlockingTest {
        val videoApod = ApodResponse(
            0, "2000-01-01", "apod", "testing", "image",
            "v1", "https://example.com","https://example.com/hd")
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(videoApod.toApod().toEntity())
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(videoApod))
        `when`(mockApodDao.insertApod(videoApod.toApod().toEntity())).thenReturn(1L)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        viewModel.videoLinkClicked()
        assertNull(viewModel.navigateVideoLink.value?.peekContent())
    }

    @Test
    fun `observe photo clicked`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        viewModel.onPhotoClicked()
        assertEquals(viewModel.navigateFullPicture.value?.peekContent(), mockApodResponse.id)
    }

    @Test
    fun `don't observe photo clicked on null apods`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadByDate(today.toString())).thenReturn(null)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        viewModel.onPhotoClicked()
        assertNull(viewModel.navigateFullPicture.value?.peekContent())
    }

    @Test
    fun `refresh updates data`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        val refreshedApodResponse = ApodResponse(
            0, "2000-02-01", "apodRefreshed", "testingRefresh",
            "image", "v1", "https://exampleRefresh.com",
            "https://exampleRefresh.com/hd"
        )
        `when`(mockApodDao.loadById(anyLong())).thenReturn(refreshedApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(refreshedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(refreshedApodResponse))
        viewModel.refresh()
        assertEquals(refreshedApodResponse.toApod(), viewModel.apod.value?.data)
    }

    @Test
    fun `update date hit`() = coroutineRule.dispatcher.runBlockingTest {
        val testDate = LocalDate.of(2000, 2, 1)
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        val updatedApodResponse = ApodResponse(
            0, "2000-02-01", "apodUpdate", "testingUpdate",
            "image", "v1", "https://exampleUpdated.com",
            "https://exampleUpdated.com/hd"
        )
        `when`(mockApodDao.loadById(anyLong())).thenReturn(updatedApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(updatedApodResponse.toApod().toEntity())).thenReturn(99L)
        `when`(mockDataSource.getApod(testDate)).thenReturn(Response.success(updatedApodResponse))
        viewModel.updateDate(testDate)
        assertEquals(updatedApodResponse.toApod(), viewModel.apod.value?.data)
    }

    @Test
    fun `refresh clears error`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        `when`(mockDataSource.getApod(today)).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        viewModel.refresh()
        assertNull(viewModel.apod.value?.error)
    }

    @Test
    fun `update clears error`() = coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(anyLong())).thenReturn(mockApodResponse.toApod().toEntity())
        `when`(mockApodDao.insertApod(mockApodResponse.toApod().toEntity())).thenReturn(1L)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        `when`(mockDataSource.getApod(today)).thenReturn(Response.error(500, "Error".toResponseBody()))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = TodayViewModel(apodRepo)
        `when`(mockDataSource.getApod(today)).thenReturn(Response.success(mockApodResponse))
        viewModel.updateDate(today)
        assertNull(viewModel.apod.value?.error)
    }
}