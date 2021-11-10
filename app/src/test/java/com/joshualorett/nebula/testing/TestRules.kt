package com.joshualorett.nebula.testing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

/**
 * Test rules.
 * Created by Joshua on 2/1/2020.
 */

// Overrides Dispatchers.Main used in Coroutines
@ExperimentalCoroutinesApi
@get:Rule
val mainCoroutineRule = MainCoroutineRule()

// Executes tasks in the Architecture Components in the same thread e.g. Room databases
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()
