package com.joshualorett.nebula.today

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import androidx.work.ListenableWorker
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before


/**
 * Test [TodaySyncWorker].
 * Created by Joshua on 2/28/2020.
 */
class TodaySyncWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSleepWorker() {
        val worker = androidx.work.testing.TestListenableWorkerBuilder<TodaySyncWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
        }
    }
}