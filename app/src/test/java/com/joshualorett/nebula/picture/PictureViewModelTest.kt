package com.joshualorett.nebula.picture

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
    private val entity = TestData.apodEntity

    @Test
    fun `gets picture from database`() =  coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(entity)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = PictureViewModel(apodRepo)
        viewModel.load(entity.id)
        assertEquals(entity.toApod().hdurl, viewModel.picture.value?.data?.hdurl)
    }

    @Test
    fun `error if database fetch fails`() =  coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(null)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao, mockImageCache)
        viewModel = PictureViewModel(apodRepo)
        viewModel.load(entity.id)
        assertNotNull(viewModel.picture.value?.error)
    }
}