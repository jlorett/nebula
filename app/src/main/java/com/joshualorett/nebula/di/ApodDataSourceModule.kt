package com.joshualorett.nebula.di

import android.content.Context
import com.joshualorett.nebula.apod.api.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * DI Module for data source.
 * Created by Joshua on 6/17/2020.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApodDataSourceModule {
    @Provides
    fun provide(@ApplicationContext context: Context): ApodDataSource {
        return ApodRemoteDataSource(
            NasaRetrofitClient,
            context.getString(R.string.key)
        )
    }
}
