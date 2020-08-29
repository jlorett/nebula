package com.joshualorett.nebula.apod.api

import com.joshualorett.nebula.RetrofitServiceDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.time.LocalDate

/**
 * Fetch an [Apod] from [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * Created by Joshua on 1/5/2020.
 */
class ApodRemoteDataSource(private val retrofitServiceDelegate: RetrofitServiceDelegate, private val key: String):
    ApodDataSource {
    override fun getApod(date: LocalDate): Flow<Response<ApodResponse>> = flow {
        val dateStr = date.toString()
        emit(retrofitServiceDelegate.create(ApodService::class.java).getApod(key, dateStr))
    }
}