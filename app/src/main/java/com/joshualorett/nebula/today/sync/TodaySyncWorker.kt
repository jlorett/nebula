package com.joshualorett.nebula.today

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.joshualorett.nebula.apod.api.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.apod.hasImage
import com.joshualorett.nebula.shared.GlideImageCache
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
            val imageCache = GlideImageCache(Dispatchers.IO)
            imageCache.attachApplicationContext(applicationContext)
           val resource = getApod(imageCache)
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

    private suspend fun getApod(imageCache: ImageCache): Resource<Apod, String> = withContext(Dispatchers.IO) {
        val dataSource = ApodRemoteDataSource(
            NasaRetrofitClient,
            applicationContext.getString(R.string.key)
        )
        val apodDao = ApodDatabaseProvider.getDatabase(applicationContext).apodDao()
        val apodRepository = ApodRepository(dataSource, apodDao, imageCache)
        val resource = apodRepository.getApod(LocalDate.now()).first()
        resource
    }
}
