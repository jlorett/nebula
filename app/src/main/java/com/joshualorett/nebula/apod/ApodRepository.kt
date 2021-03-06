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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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
    private var dispatcher: CoroutineDispatcher = Dispatchers.Default

    constructor(
        apodService: ApodService,
        apodDao: ApodDao,
        imageCache: ImageCache,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(apodService, apodDao, imageCache) {
        this.dispatcher = dispatcher
    }

    /***
     * Fetch [Apod] by date. This will attempt to get it from the database first and if not found,
     * will fetch from the api.
     */
    fun getApod(date: LocalDate): Flow<Resource<Apod, String>> {
        if (date.isBefore(earliestDate)) {
            return flowOf(Resource.Error("Date can't be before $earliestDate."))
        }
        return apodDao.loadByDate(date.toString())
            .map { cachedEntity ->
                val cachedApod = cachedEntity?.toApod()
                if (cachedApod == null) {
                    val response = getApodByDataService(date)
                    if (response.successful()) {
                        getCachedApod(response.data ?: 0L)
                    } else {
                        Resource.Error(response.error ?: "Error getting Apod from api.")
                    }
                } else {
                    Resource.Success(cachedApod)
                }
            }.flowOn(dispatcher)
    }

    fun getApod(id: Long): Flow<Resource<Apod, String>> = flow {
        emit(getCachedApod(id))
    }.flowOn(dispatcher)

    /***
     * Fetch a fresh [Apod] by date. This will always attempt to fetch the apod from the api.
     */
    fun getFreshApod(date: LocalDate): Flow<Resource<Apod, String>> = flow {
        if (date.isBefore(earliestDate)) {
            emit(Resource.Error("Date can't be before $earliestDate."))
        }
        val response = getApodByDataService(date)
        if (response.successful()) {
            emit(getCachedApod(response.data ?: 0L))
        } else {
            emit(Resource.Error(response.error ?: "Error getting Apod from api."))
        }
    }.flowOn(dispatcher)

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

    private suspend fun getCachedApod(id: Long): Resource<Apod, String> {
        val cachedEntity = apodDao.loadById(id).first()?.toApod()
        return if (cachedEntity == null) {
            Resource.Error("Apod not found in database.")
        } else {
            Resource.Success(cachedEntity)
        }
    }
}
