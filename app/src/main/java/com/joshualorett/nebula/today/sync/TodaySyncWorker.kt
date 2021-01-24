package com.joshualorett.nebula.today.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.joshualorett.nebula.apod.api.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.hasImage
import com.joshualorett.nebula.di.ApodDaoModule
import com.joshualorett.nebula.di.ImageCacheModule
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Syncs Today's Astronomy Picture of the Day in the background.
 * Created by Joshua on 2/8/2020.
 */
class TodaySyncWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    private val maxRetryAttempts = 2

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val imageCache = ImageCacheModule.provide()
            imageCache.attachApplicationContext(applicationContext)
           val resource = getApod(applicationContext, imageCache)
            when {
                resource.successful() -> {
                    val apod = (resource as Resource.Success).data
                    if(apod.hasImage()) {
                        val cacheSuccessful = imageCache.cache(apod.hdurl ?: apod.url)
                        if(cacheSuccessful) Result.success() else Result.failure()
                    } else {
                        Result.success()
                    }
                }
                runAttemptCount < maxRetryAttempts -> {
                    Result.retry()
                }
                else -> {
                    Result.failure()
                }
            }.also {
                imageCache.detachApplicationContext()
            }
        }
    }

    private suspend fun getApod(context: Context, imageCache: ImageCache): Resource<Apod, String> = withContext(Dispatchers.IO) {
        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            applicationContext.getString(R.string.key)
        )
        val apodDao = ApodDaoModule.provideDatabase(context).apodDao()
        val apodRepository = ApodRepository(dataSource, apodDao, imageCache)
        val resource = apodRepository.getApod(LocalDate.now()).first()
        resource
    }
}