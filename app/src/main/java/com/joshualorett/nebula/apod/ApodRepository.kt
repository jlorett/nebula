package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.shared.data
import com.joshualorett.nebula.shared.error
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository @Inject constructor(
    private val apodService: ApodService,
    private val apodDao: ApodDao,
    private val imageCache: ImageCache
) {
    // The first APOD was 1995-06-16.
    private val earliestDate: LocalDate = LocalDate.of(1995, 6, 16)

    /***
     * Fetch [Apod] by date. This will attempt to get it from the database first and if not found,
     * will fetch from the api.
     */
    suspend fun getApod(date: LocalDate): Resource<Apod, String> {
        if (date.isBefore(earliestDate)) {
            return Resource.Error("Date can't be before $earliestDate.")
        }
        val cachedEntity = apodDao.loadByDate(date.toString())
        val cachedApod = cachedEntity?.toApod()
        return if (cachedApod == null) {
            val response = getApodByDataService(date)
            if (response.successful()) {
                getCachedApod(response.data ?: 0L)
            } else {
                Resource.Error(response.error ?: "Error getting Apod from api.")
            }
        } else {
            Resource.Success(cachedApod)
        }
    }

    /***
     * Fetch a fresh [Apod] by date. This will always attempt to fetch the apod from the api.
     */
    suspend fun getFreshApod(date: LocalDate): Resource<Apod, String> {
        if (date.isBefore(earliestDate)) {
            return Resource.Error("Date can't be before $earliestDate.")
        }
        val response = getApodByDataService(date)
        return if (response.successful()) {
            getCachedApod(response.data ?: 0L)
        } else {
            Resource.Error(response.error ?: "Error getting Apod from api.")
        }
    }

    /***
     * Fetch a cached [Apod] by id. This will always attempt to fetch the apod from the database.
     */
    suspend fun getCachedApod(id: Long): Resource<Apod, String> {
        val cachedEntity = apodDao.loadById(id)?.toApod()
        return if (cachedEntity == null) {
            Resource.Error("Apod not found in database.")
        } else {
            Resource.Success(cachedEntity)
        }
    }

    suspend fun hasCachedApod(date: LocalDate): Boolean {
        if (date.isBefore(earliestDate)) {
            return false
        }
        return apodDao.loadByDate(date.toString()) != null
    }

    suspend fun clearCache() {
        apodDao.deleteAll()
        imageCache.clear()
    }

    private suspend fun getApodByDataService(date: LocalDate): Resource<Long, String> {
        try {
            val response = apodService.getApod(date.toString())
            return if (response.isSuccessful) {
                val networkApod = response.body()?.toApod()
                if (networkApod == null) {
                    Resource.Error("Empty network body.")
                } else {
                    Resource.Success(cacheApod(networkApod))
                }
            } else {
                Resource.Error("Error getting apod with status ${response.code()}.")
            }
        } catch (ioException: IOException) {
            return Resource
                .Error("Your network is unavailable. Check your data or wifi connection.")
        } catch (exception: Exception) {
            return Resource.Error("An error occurred. $exception")
        }
    }

    private suspend fun cacheApod(apod: Apod): Long {
        apodDao.delete(apod.toEntity())
        return apodDao.insertApod(apod.toEntity())
    }
}
