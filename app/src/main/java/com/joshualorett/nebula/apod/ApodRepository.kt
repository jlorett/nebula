package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import java.io.IOException
import java.time.LocalDate

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource, private val apodDao: ApodDao,
                     private val imageCache: ImageCache) {
    // The first APOD was 1995-06-16.
    private val earliestDate: LocalDate = LocalDate.of(1995, 6, 16)

    /***
     * Fetch [Apod] by date. This will attempt to get it from the database first and if not found,
     * will fetch from the api.
     */
    suspend fun getApod(date: LocalDate): Resource<Apod, String> {
        if (date.isBefore(earliestDate)) {
            return Resource.Error("Date can't be before 1995-06-16.")
        }
        val cachedApod = apodDao.loadByDate(date.toString())?.toApod()
        return if (cachedApod == null) {
            getApodByDataSource(date)
        } else {
            Resource.Success(cachedApod)
        }
    }

    /***
     * Fetch a fresh [Apod] by date. This will always attempt to fetch the apod from the api.
     */
    suspend fun getFreshApod(date: LocalDate): Resource<Apod, String> {
        if (date.isBefore(earliestDate)) {
            return Resource.Error("Date can't be before 1995-06-16.")
        }
        return getApodByDataSource(date)
    }

    /***
     * This will fetch an [Apod] from the database by its id.
     */
    suspend fun getCachedApod(id: Long): Resource<Apod, String> {
        val cachedApod = apodDao.loadById(id)?.toApod()
        return if (cachedApod == null) {
            Resource.Error("Apod not found in database.")
        } else {
            Resource.Success(cachedApod)
        }
    }

    private suspend fun getApodByDataSource(date: LocalDate): Resource<Apod, String> {
        try {
            val response = apodDataSource.getApod(date)
            return if (response.isSuccessful) {
                val networkApod = response.body()?.toApod()
                if (networkApod == null) {
                    Resource.Error("Empty network body.")
                } else {
                    return cacheApod(networkApod)
                }
            } else {
                Resource.Error("Error getting apod with status ${response.code()}.")
            }
        } catch (e: IOException) {
            return Resource.Error("Your network is unavailable. Check your data or wifi connection.")
        }
    }

    private suspend fun cacheApod(apod: Apod): Resource<Apod, String> {
        apodDao.delete(apod.toEntity())
        val id = apodDao.insertApod(apod.toEntity())
        val cachedApod = apodDao.loadById(id)?.toApod()
        return if (cachedApod == null) {
            Resource.Error("Error loading apod from database.")
        } else {
            Resource.Success(cachedApod)
        }
    }

    /***
     * Clear out resources.
     */
    suspend fun clearResources() {
        apodDao.deleteAll()
        imageCache.clear()
    }
}