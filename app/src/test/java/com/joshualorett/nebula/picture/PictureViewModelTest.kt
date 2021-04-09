package com.joshualorett.nebula.picture

import androidx.lifecycle.*
import com.joshualorett.nebula.FakeLifecycleOwner
import com.joshualorett.nebula.TestData
import com.joshualorett.nebula.ViewModelTest
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
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
class PictureViewModelTest: ViewModelTest() {
    private lateinit var viewModel: PictureViewModel
    private val mockApodService = mock(ApodService::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val entity = TestData.apodEntity
    private val lifecycleOwner = FakeLifecycleOwner()

    @Test
    fun getsPictureFromDatabase() =  coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(flowOf(entity))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        lifecycleOwner.setState(Lifecycle.State.STARTED)
        viewModel = PictureViewModel(apodRepo, SavedStateHandle(mapOf("id" to entity.id)), coroutineRule.dispatcher)
        viewModel.picture
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                assertEquals(entity.toApod().hdurl, it.data?.hdurl)
            }
            .launchIn(lifecycleOwner.lifecycleScope)
        lifecycleOwner.setState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun errorIfDatabaseFetchFails() =  coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(flowOf(null))
        val apodRepo = ApodRepository(mockApodService, mockApodDao, mockImageCache)
        lifecycleOwner.setState(Lifecycle.State.STARTED)
        viewModel = PictureViewModel(apodRepo, SavedStateHandle(mapOf("id" to entity.id)), coroutineRule.dispatcher)
        viewModel.picture
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                assertNotNull(viewModel.picture.conflate().first().error)
            }
            .launchIn(lifecycleOwner.lifecycleScope)
        lifecycleOwner.setState(Lifecycle.State.DESTROYED)
    }
}