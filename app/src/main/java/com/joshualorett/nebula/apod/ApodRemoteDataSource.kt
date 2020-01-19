package com.joshualorett.nebula.apod

import com.joshualorett.nebula.RetrofitServiceDelegate
import retrofit2.Response
import java.time.LocalDate

/**
 * Fetch an [Apod] from [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * Created by Joshua on 1/5/2020.
 */
class ApodRemoteDataSource(private val retrofitServiceDelegate: RetrofitServiceDelegate, private val key: String): ApodDataSource {
    override suspend fun getApod(date: LocalDate): Response<ApodResponse> {
        val dateStr = date.toString()
        return retrofitServiceDelegate.create(ApodService::class.java)
            .getApod(key, dateStr)
    }
}