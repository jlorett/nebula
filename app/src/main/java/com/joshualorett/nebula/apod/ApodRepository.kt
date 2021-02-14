package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.shared.data
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository @Inject constructor(private val apodDataSource: ApodDataSource,
                                         private val apodDao: ApodDao,
                                         private val imageCache: ImageCache) {
    // The first APOD was 1995-06-16.
    private val earliestDate: LocalDate = LocalDate.of(1995, 6, 16)

    /***
     * Fetch [Apod] by date. This will attempt to get it from the database first and if not found,
     * will fetch from the api.
     */
    fun getApod(date: LocalDate): Flow<Resource<Apod, String>> {
        if (date.isBefore(earliestDate)) {
            return flowOf(Resource.Error("Date can't be before $earliestDate."))
        }
        return apodDao.loadByDate(date.toString())
            .map { entity ->
                val cachedApod = entity?.toApod()
                if (cachedApod == null) {
                    val id = getApodByDataSource(date).data ?: 0
                    getCachedApod(id)
                } else {
                    Resource.Success(cachedApod)
                }
            }
    }

    /***
     * Fetch a fresh [Apod] by date. This will always attempt to fetch the apod from the api.
     */
    fun getFreshApod(date: LocalDate): Flow<Resource<Apod, String>> = flow {
        if (date.isBefore(earliestDate)) {
            emit(Resource.Error("Date can't be before $earliestDate."))
        }
        val id = getApodByDataSource(date).data ?: 0
        emit(getCachedApod(id))
    }

    suspend fun getCachedApod(id: Long): Resource<Apod, String> {
        val cachedEntity = apodDao.loadById(id).first()?.toApod()
        return if(cachedEntity == null) {
            Resource.Error("Apod not found in database.")
        } else {
            Resource.Success(cachedEntity)
        }
    }

    suspend fun clearResources() {
        apodDao.deleteAll()
        imageCache.clear()
    }

    private suspend fun getApodByDataSource(date: LocalDate): Resource<Long, String> {
        try {
            val response = apodDataSource.getApod(date)
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
            return Resource.Error("Your network is unavailable. Check your data or wifi connection.")
        } catch (exception: Exception) {
            return Resource.Error("An error occurred. $exception")
        }
    }

    private suspend fun cacheApod(apod: Apod): Long {
        apodDao.delete(apod.toEntity())
        return apodDao.insertApod(apod.toEntity())
    }
}