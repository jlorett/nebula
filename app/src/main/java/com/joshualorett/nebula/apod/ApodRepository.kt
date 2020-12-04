package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.flow.*
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
            return flowOf(Resource.Error("Date can't be before 1995-06-16."))
        }
        return apodDao.loadByDate(date.toString())
            .flatMapLatest { entity ->
                val cachedApod = entity?.toApod()
                if (cachedApod == null) {
                    getApodByDataSource(date)
                } else {
                    flowOf(Resource.Success(cachedApod))
                }
            }
    }

    /***
     * Fetch a fresh [Apod] by date. This will always attempt to fetch the apod from the api.
     */
    fun getFreshApod(date: LocalDate): Flow<Resource<Apod, String>> {
        if (date.isBefore(earliestDate)) {
            return flowOf(Resource.Error("Date can't be before 1995-06-16."))
        }
        return getApodByDataSource(date)
    }

    /***
     * This will fetch an [Apod] from the database by its id.
     */
    fun getCachedApod(id: Long): Flow<Resource<Apod, String>> {
        return apodDao.loadById(id)
            .map { entity ->
                val cachedApod = entity?.toApod()
                if (cachedApod == null) {
                    Resource.Error("Apod not found in database.")
                } else {
                    Resource.Success(cachedApod)
                }
            }
    }

    private fun getApodByDataSource(date: LocalDate): Flow<Resource<Apod, String>> {
        return apodDataSource.getApod(date).flatMapLatest { response ->
            if (response.isSuccessful) {
                val networkApod = response.body()?.toApod()
                if (networkApod == null) {
                    flowOf(Resource.Error("Empty network body."))
                } else {
                    val id = cacheApod(networkApod)
                    getCachedApod(id)
                }
            } else {
                flowOf(Resource.Error("Error getting apod with status ${response.code()}."))
            }
        }.catch {
            emit(Resource.Error("Your network is unavailable. Check your data or wifi connection."))
        }
    }

    /***
     * Cache the apod and return the auto generated primary key.
     */
    private suspend fun cacheApod(apod: Apod): Long {
        apodDao.delete(apod.toEntity())
        return apodDao.insertApod(apod.toEntity())
    }

    /***
     * Clear out resources.
     */
    suspend fun clearResources() {
        apodDao.deleteAll()
        imageCache.clear()
    }
}