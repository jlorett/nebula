package com.joshualorett.nebula.testing

import androidx.annotation.VisibleForTesting
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Inherit from this class when testing ViewModels.
 * Created by Joshua on 2/1/2020.
 */
@ExperimentalCoroutinesApi
open class ViewModelTest {
    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    val test = InstantTaskExecutorRule()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun <T> LiveData<T>.getOrAwaitValue(
        skip: Int = 0,
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        val latchCount = 1 + skip
        var data: T? = null
        val latch = CountDownLatch(latchCount)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                // CountDownLatch takes an int for count but returns a long?
                if(latch.count.toInt() == latchCount ) {
                    this@getOrAwaitValue.removeObserver(this)
                }
            }
        }
        this.observeForever(observer)
        try {
            // Don't wait indefinitely if the LiveData is not set.
            if (!latch.await(time, timeUnit)) {
                if(latchCount > 1) {
                    throw TimeoutException("LiveData value was never set after skipping $skip times.")
                } else {
                    throw TimeoutException("LiveData value was never set.")
                }
            }
        } finally {
            this.removeObserver(observer)
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}