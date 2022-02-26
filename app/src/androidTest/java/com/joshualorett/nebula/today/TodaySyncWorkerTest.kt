package com.joshualorett.nebula.today

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.di.ApodDaoModule
import com.joshualorett.nebula.di.ApodServiceModule
import com.joshualorett.nebula.di.ImageCacheModule
import com.joshualorett.nebula.sync.TodaySyncWorker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test [TodaySyncWorker].
 * Created by Joshua on 2/28/2020.
 */
class TodaySyncWorkerTest {

    private lateinit var context: Context
    val imageCache = ImageCacheModule.provide()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        imageCache.attachApplicationContext(context)
    }

    @Test
    fun testSleepWorker() {

        val worker = TestListenableWorkerBuilder<TodaySyncWorker>(context)
            .setWorkerFactory(object: WorkerFactory(){
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    val dataService = ApodServiceModule.provide(appContext)
                    val apodDao = ApodDaoModule.provideDatabase(appContext).apodDao()
                    val apodRepository = ApodRepository(dataService, apodDao, imageCache)
                    return TodaySyncWorker(appContext, workerParameters, imageCache, apodRepository)
                }
            })
            .build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
        }
    }
}
