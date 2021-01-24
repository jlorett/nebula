package com.joshualorett.nebula.picture

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.joshualorett.nebula.TestData
import com.joshualorett.nebula.ViewModelTest
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.toApod
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    private val mockDataSource = mock(ApodDataSource::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val mockImageCache = mock(ImageCache::class.java)
    private val lifecycleOwner = mock(LifecycleOwner::class.java)
    private val lifecycle = mock(Lifecycle::class.java)
    private val entity = TestData.apodEntity

    @Test
    fun getsPictureFromDatabase() =  coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(flowOf(entity))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        `when`(lifecycle.currentState).thenReturn(Lifecycle.State.RESUMED)
        `when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)
        viewModel = PictureViewModel(apodRepo, coroutineRule.dispatcher)
        viewModel.load(entity.id)
        assertEquals(entity.toApod().hdurl, viewModel.picture.getOrAwaitValue(1).data?.hdurl)
    }

    @Test
    fun errorIfDatabaseFetchFails() =  coroutineRule.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(flowOf(null))
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        `when`(lifecycle.currentState).thenReturn(Lifecycle.State.RESUMED)
        `when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)
        viewModel = PictureViewModel(apodRepo, coroutineRule.dispatcher)
        viewModel.load(entity.id)
        assertNotNull(viewModel.picture.getOrAwaitValue(1).error)
    }
}