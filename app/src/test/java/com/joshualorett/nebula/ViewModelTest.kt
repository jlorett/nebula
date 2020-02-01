package com.joshualorett.nebula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

/**
 * Inherit from this class when testing ViewModels.
 * Created by Joshua on 2/1/2020.
 */
@ExperimentalCoroutinesApi
open class ViewModelTest {
    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    val coroutineRule = TestCoroutineRule()

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    val test = InstantTaskExecutorRule()
}