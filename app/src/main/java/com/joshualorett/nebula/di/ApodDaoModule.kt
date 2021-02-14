package com.joshualorett.nebula.di

import android.content.Context
import androidx.room.Room
import com.joshualorett.nebula.apod.database.ApodDao
import com.joshualorett.nebula.apod.database.ApodDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI Module for database.
 * Created by Joshua on 6/17/2020.
 */

@Module
@InstallIn(SingletonComponent::class)
object ApodDaoModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext applicationContext: Context): ApodDatabase {
        return  Room.databaseBuilder(applicationContext,
            ApodDatabase::class.java,
            "apod").build()
    }

    @Provides
    fun provideDao(database: ApodDatabase): ApodDao {
        return database.apodDao()
    }
}