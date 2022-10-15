package com.joshualorett.nebula.di

import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.apod.api.ApodService
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.shared.ImageCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApodRepositoryModule {
    @Provides
    fun provide(
        apodService: ApodService,
        apodDao: ApodDao,
        imageCache: ImageCache
    ): ApodRepository {
        return ApodRepository(apodService, apodDao, imageCache)
    }
}
