package com.joshualorett.nebula.picture

import androidx.lifecycle.SavedStateHandle
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import com.joshualorett.nebula.testing.TestData
import com.joshualorett.nebula.ui.picture.PictureViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * Test [PictureViewModel].
 * Created by Joshua on 1/23/2020.
 */
@ExperimentalCoroutinesApi
class PictureViewModelTest {
    private lateinit var apodRepo: ApodRepository
    private lateinit var viewModel: PictureViewModel
    private val mockApodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val entity = TestData.apodEntity

    @Before
    fun setup() {
        apodRepo = ApodRepository(
            mockApodService,
            mockApodDao,
            mockImageCache
        )
    }

    @Test
    fun getsPictureFromDatabase() = runTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(entity)
        viewModel = PictureViewModel(apodRepo, SavedStateHandle(mapOf("id" to entity.id)))
        val job = launch {
            val url = viewModel.picture.last().data?.hdurl
            assertEquals(entity.toApod().hdurl, url)
        }
        job.cancel()
    }

    @Test
    fun errorIfDatabaseFetchFails() = runTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(null)
        viewModel = PictureViewModel(apodRepo, SavedStateHandle(mapOf("id" to entity.id)))
        val job = launch {
            val error = viewModel.picture.last().error
            assertNotNull(error)
        }
        job.cancel()
    }
}
