package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.Resource
import java.time.LocalDate

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource, private val apodDao: ApodDao) {
    // The first APOD was 1995-06-16, month is 0 based.
    private val earliestDate: LocalDate = LocalDate.of(1995, 6, 16)

    suspend fun getApod(date: LocalDate): Resource<Apod> {
        if (date.isBefore(earliestDate)) {
            return Resource.Error("Date can't be before 1995-06-16.")
        }
        val cachedApod = apodDao.loadByDate(date.toString())?.toApod()
        return if (cachedApod == null) {
            val response = apodDataSource.getApod(date)
            if (response.isSuccessful) {
                getApodByDataSource(date)
            } else {
                Resource.Error("Error getting apod with status ${response.code()}.")
            }
        } else {
            Resource.Success(cachedApod)
        }
    }

    private suspend fun getApodByDataSource(date: LocalDate): Resource<Apod> {
        val response = apodDataSource.getApod(date)
        return if (response.isSuccessful) {
            val networkApod = response.body()?.toApod()
            if (networkApod == null) {
                Resource.Error("Empty network body.")
            } else {
                // Only keep one apod in cache at a time
                apodDao.deleteAll()
                return cacheApod(networkApod)
            }
        } else {
            Resource.Error("Error getting apod with status ${response.code()}.")
        }
    }

    private suspend fun cacheApod(apod: Apod): Resource<Apod> {
        val id = apodDao.insertApod(apod.toEntity())
        val cachedApod = apodDao.loadById(id)?.toApod()
        return if (cachedApod == null) {
            Resource.Error("Error loading apod from cache.")
        } else {
            Resource.Success(cachedApod)
        }
    }
}