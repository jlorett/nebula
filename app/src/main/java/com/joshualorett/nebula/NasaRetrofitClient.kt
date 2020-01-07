package com.joshualorett.nebula

import retrofit2.Retrofit

/**
 * Retrofit instance for Nasa Api.
 * Created by Joshua on 1/7/2020.
 */
object NasaRetrofitClient: RetrofitServiceDelegate {
    val retrofit: Retrofit =  Retrofit.Builder()
        .baseUrl("https://api.nasa.gov/")
        .build()

    override fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}