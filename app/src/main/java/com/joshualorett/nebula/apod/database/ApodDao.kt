package com.joshualorett.nebula.apod.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access model for Apod database.
 * Created by Joshua on 1/12/2020.
 */
@Dao
interface ApodDao {
    @Query("SELECT * FROM apodentity WHERE date LIKE :date LIMIT 1")
    fun loadByDate(date: String): Flow<ApodEntity?>

    @Query("SELECT * FROM apodentity WHERE id = :id LIMIT 1")
    fun loadById(id: Long): Flow<ApodEntity?>

    @Insert
    suspend fun insertApod(apodEntity: ApodEntity): Long

    @Delete
    suspend fun delete(apodEntity: ApodEntity)

    @Query("DELETE FROM apodentity")
    suspend fun deleteAll()
}