package com.joshualorett.nebula.di

import com.joshualorett.nebula.shared.GlideImageCache
import com.joshualorett.nebula.shared.ImageCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

/**
 * DI Module for image cache.
 * Created by Joshua on 6/17/2020.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageCacheModule {
    @Singleton
    @Provides
    fun provide(): ImageCache {
        return GlideImageCache(Dispatchers.Default)
    }
}
