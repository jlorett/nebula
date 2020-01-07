package com.joshualorett.nebula.apod

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Fetch an [Apod] from [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * @param key Nasa api key.
 * @param date The date of the Astronomy Picture of the Day.
 * Created by Joshua on 1/4/2020.
 */
interface ApodService {
    @GET("planetary/apod")
    suspend fun getApod(@Query("api_key") key: String, @Query("date") date: String): Response<Apod>
}