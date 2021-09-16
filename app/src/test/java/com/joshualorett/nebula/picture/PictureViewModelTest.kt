package com.joshualorett.nebula.picture

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import com.joshualorett.nebula.testing.TestData
import com.joshualorett.nebula.testing.ViewModelTest
import com.joshualorett.nebula.ui.picture.PictureViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * Test [PictureViewModel].
 * Created by Joshua on 1/23/2020.
 */
@ExperimentalCoroutinesApi
class PictureViewModelTest : ViewModelTest() {
    private lateinit var viewModel: PictureViewModel
    private val mockApodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val entity = TestData.apodEntity

    @Test
    fun getsPictureFromDatabase() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(entity)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = PictureViewModel(apodRepo, SavedStateHandle(mapOf("id" to entity.id)))
        val url = viewModel.picture.conflate().first().data?.hdurl
        assertEquals(entity.toApod().hdurl, url)
    }

    @Test
    fun errorIfDatabaseFetchFails() = coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(null)
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache, coroutineRule.dispatcher)
        viewModel = PictureViewModel(apodRepo, SavedStateHandle(mapOf("id" to entity.id)))
        val error = viewModel.picture.conflate().first().error
        assertNotNull(error)
    }
}
