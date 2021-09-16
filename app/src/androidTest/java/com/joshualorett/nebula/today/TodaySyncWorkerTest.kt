package com.joshualorett.nebula.today

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import com.joshualorett.nebula.ui.today.sync.TodaySyncWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
        val worker = androidx.work.testing.TestListenableWorkerBuilder<TodaySyncWorker>(context)
            .build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
        }
    }
}
