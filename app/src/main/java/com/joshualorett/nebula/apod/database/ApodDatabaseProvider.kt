package com.joshualorett.nebula.apod.database

import android.content.Context
import androidx.room.Room

/**
 * Provides [ApodDatabase] instance.
 * Created by Joshua on 1/12/2020.
 */
object ApodDatabaseProvider {
    private lateinit var apodDatabase: ApodDatabase

    fun getDatabase(applicationContext: Context): ApodDatabase {
        if(!ApodDatabaseProvider::apodDatabase.isInitialized) {
            apodDatabase = Room.databaseBuilder(
                applicationContext,
                ApodDatabase::class.java, "apod"
            ).build()
        }
        return apodDatabase
    }
}