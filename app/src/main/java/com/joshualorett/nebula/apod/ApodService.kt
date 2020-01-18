package com.joshualorett.nebula.apod

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * Created by Joshua on 1/4/2020.
 */
interface ApodService {
    /***
     * Get an [ApodResponse] from the api.
     * @param key Nasa api key.
     * @param date The date of the Astronomy Picture of the Day.
     */
    @GET("planetary/apod")
    suspend fun getApod(@Query("api_key") key: String, @Query("date") date: String): Response<ApodResponse>
}