package com.joshualorett.nebula.apod.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Apod model for the database.
 * Created by Joshua on 1/12/2020.
 */
@Entity
data class ApodEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "explanation") val explanation: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "hdurl") val hdurl: String?,
    @ColumnInfo(name = "copyright") val copyright: String?
)