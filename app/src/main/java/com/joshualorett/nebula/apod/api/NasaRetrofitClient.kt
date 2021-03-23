package com.joshualorett.nebula.apod.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.joshualorett.nebula.shared.RetrofitServiceDelegate
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit instance for Nasa API.
 * Created by Joshua on 1/7/2020.
 */
object NasaRetrofitClient: RetrofitServiceDelegate {
    override fun <T> create(key: String, service: Class<T>): T {
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        val httpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(ApodAuthInterceptor(key))
            .build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
        return retrofit.create(service)
    }
}