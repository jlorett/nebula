package com.joshualorett.nebula.di

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.api.ApodAuthInterceptor
import com.joshualorett.nebula.apod.api.ApodService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * DI Module for data service.
 * Created by Joshua on 6/17/2020.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApodServiceModule {
    @Provides
    fun provide(@ApplicationContext context: Context): ApodService {
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        val httpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(ApodAuthInterceptor(context.getString(R.string.key)))
            .build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
        return retrofit.create(ApodService::class.java)
    }
}
