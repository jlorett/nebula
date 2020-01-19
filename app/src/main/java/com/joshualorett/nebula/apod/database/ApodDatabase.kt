package com.joshualorett.nebula.apod.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * [ApodEntity] database.
 * Created by Joshua on 1/12/2020.
 */
@Database(entities = [ApodEntity::class], version = 1)
abstract class ApodDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao
}
