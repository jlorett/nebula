package com.joshualorett.nebula.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.*
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.hasImage
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Syncs Today's Astronomy Picture of the Day in the background.
 * Created by Joshua on 2/8/2020.
 */
@HiltWorker
class TodaySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val imageCache: ImageCache,
    private val apodRepository: ApodRepository
) : CoroutineWorker(context, params) {
    private val maxRetryAttempts = 2

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val alreadyCached = apodRepository.hasCachedApod(LocalDate.now())
            if (alreadyCached) {
                return@withContext Result.success()
            }
            imageCache.attachApplicationContext(applicationContext)
            val resource = getApod(apodRepository)
            when {
                resource.successful() -> {
                    val apod = (resource as Resource.Success).data
                    if (apod.hasImage()) {
                        val cacheSuccessful = imageCache.cache(apod.hdurl ?: apod.url)
                        if (cacheSuccessful) Result.success() else Result.failure()
                    } else {
                        Result.success()
                    }
                }
                runAttemptCount < maxRetryAttempts -> { Result.retry() }
                else -> { Result.failure() }
            }.also {
                imageCache.detachApplicationContext()
            }
        }
    }

    private suspend fun getApod(apodRepository: ApodRepository):
        Resource<Apod, String> = withContext(Dispatchers.IO) {
        val resource = apodRepository.getApod(LocalDate.now())
        resource
    }
}

fun setupRecurringSyncWork(context: Context) {
    val unmetered = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(context.getString(R.string.settings_key_unmetered), false)
    val workConstraints = Constraints
        .Builder()
        .setRequiredNetworkType(
            if (unmetered) NetworkType.UNMETERED
            else NetworkType.CONNECTED
        )
        .build()
    val syncRequest = PeriodicWorkRequestBuilder<TodaySyncWorker>(1, TimeUnit.HOURS)
        .setConstraints(workConstraints)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        TodaySyncWorker::class.java.simpleName,
        ExistingPeriodicWorkPolicy.REPLACE,
        syncRequest
    )
}

fun cancelRecurringSyncWork(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(TodaySyncWorker::class.java.simpleName)
}
