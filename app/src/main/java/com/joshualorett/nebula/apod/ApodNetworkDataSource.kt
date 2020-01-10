package com.joshualorett.nebula.apod

import com.joshualorett.nebula.RetrofitServiceDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.util.*

/**
 * Fetch an [Apod] from [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * Created by Joshua on 1/5/2020.
 */
class ApodNetworkDataSource(private val retrofitServiceDelegate: RetrofitServiceDelegate, private val key: String): ApodDataSource {
    // The first APOD was 1995-06-16, month is 0 based.
    private val earliestDate: Date = Date(1995, 5, 16)

    override suspend fun getApod(date: Date): Flow<Response<Apod>> {
        if (date.before(earliestDate)) {
            throw IllegalArgumentException("Date can't be before 1995-06-16")
        } else {
            return flow {
                val response = retrofitServiceDelegate.create(ApodService::class.java)
                    .getApod(key, date.toString())
                emit(response)
            }
        }
    }
}