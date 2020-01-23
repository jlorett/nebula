package com.joshualorett.nebula.picture

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.joshualorett.nebula.TestCoroutineRule
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.database.ApodEntity
import com.joshualorett.nebula.apod.toApod
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNotNull
import org.junit.Test

import org.junit.Rule
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * Test [PictureViewModel].
 * Created by Joshua on 1/23/2020.
 */
@ExperimentalCoroutinesApi
class PictureViewModelTest {
    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    val coroutineRule = TestCoroutineRule()

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    val test = InstantTaskExecutorRule()

    private lateinit var viewModel: PictureViewModel
    private val mockDataSource = mock(ApodDataSource::class.java)
    private val mockApodDao = mock(ApodDao::class.java)
    private val entity = ApodEntity(
        1L, "2000-01-01", "apod", "testing",
        "image", "v1", "https://example.com",
        "https://example.com/hd")

    @Test
    fun `gets picture from database`() =  coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(entity)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = PictureViewModelFactory(apodRepo, entity.id).create(PictureViewModel::class.java)
        assertEquals(entity.toApod().hdurl, viewModel.picture.value)
    }

    @Test
    fun `error if database fetch fails`() =  coroutineRule.dispatcher.runBlockingTest {
        `when`(mockApodDao.loadById(entity.id)).thenReturn(null)
        val apodRepo = ApodRepository(mockDataSource, mockApodDao)
        viewModel = PictureViewModelFactory(apodRepo, entity.id).create(PictureViewModel::class.java)
        assertNotNull(viewModel.error.value)
    }
}