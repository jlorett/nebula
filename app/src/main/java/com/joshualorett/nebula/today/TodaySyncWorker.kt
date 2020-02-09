package com.joshualorett.nebula.today

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.joshualorett.nebula.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import com.joshualorett.nebula.apod.database.ApodDatabaseProvider
import com.joshualorett.nebula.shared.GlideImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Syncs Today's Astronomy Picture of the Day in the background.
 * Created by Joshua on 2/8/2020.
 */
class TodaySyncWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    private val maxRetryAttempts = 2
    private var retryAttempt = 0

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val imageCache = GlideImageCache(Dispatchers.Default)
            imageCache.attachApplicationContext(applicationContext)
            val dataSource = ApodRemoteDataSource(
                NasaRetrofitClient,
                applicationContext.getString(R.string.key)
            )
            val apodDao = ApodDatabaseProvider.getDatabase(applicationContext).apodDao()
            val apodRepository = ApodRepository(dataSource, apodDao, imageCache)
            val resource = apodRepository.getApod(LocalDate.now())
            imageCache.detachApplicationContext()
            when {
                resource.successful() -> {
                    retryAttempt = 0
                    Result.success()
                }
                retryAttempt >= maxRetryAttempts -> {
                    retryAttempt.inc()
                    Result.retry()
                }
                else -> {
                    retryAttempt = 0
                    Result.failure()
                }
            }
        }
    }
}
