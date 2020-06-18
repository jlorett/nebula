package com.joshualorett.nebula.di

import android.content.Context
import com.joshualorett.nebula.NasaRetrofitClient
import com.joshualorett.nebula.R
import com.joshualorett.nebula.apod.api.ApodDataSource
import com.joshualorett.nebula.apod.api.ApodRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * DI Module for data source.
 * Created by Joshua on 6/17/2020.
 */
@Module
@InstallIn(ApplicationComponent::class)
object ApodDataSourceModule {
    @Provides
    fun provide(@ApplicationContext context: Context): ApodDataSource {
        return ApodRemoteDataSource(
            NasaRetrofitClient,
            context.getString(R.string.key)
        )
    }
}
