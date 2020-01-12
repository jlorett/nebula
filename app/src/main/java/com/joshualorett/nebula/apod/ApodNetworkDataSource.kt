package com.joshualorett.nebula.apod

import com.joshualorett.nebula.RetrofitServiceDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fetch an [Apod] from [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * Created by Joshua on 1/5/2020.
 */
class ApodNetworkDataSource(private val retrofitServiceDelegate: RetrofitServiceDelegate, private val key: String): ApodDataSource {
    override suspend fun getApod(date: Date): Flow<Response<Apod>> {
        return flow {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            val response = retrofitServiceDelegate.create(ApodService::class.java)
                .getApod(key, dateStr)
            emit(response)
        }
    }
}