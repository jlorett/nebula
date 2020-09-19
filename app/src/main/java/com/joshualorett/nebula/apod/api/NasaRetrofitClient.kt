package com.joshualorett.nebula.apod.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.joshualorett.nebula.shared.RetrofitServiceDelegate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit instance for Nasa API.
 * Created by Joshua on 1/7/2020.
 */
object NasaRetrofitClient: RetrofitServiceDelegate {
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val retrofit: Retrofit =  Retrofit.Builder()
        .baseUrl("https://api.nasa.gov/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    override fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}