package com.joshualorett.nebula

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit instance for Nasa API.
 * Created by Joshua on 1/7/2020.
 */
object NasaRetrofitClient: RetrofitServiceDelegate {
    val retrofit: Retrofit =  Retrofit.Builder()
        .baseUrl("https://api.nasa.gov/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}