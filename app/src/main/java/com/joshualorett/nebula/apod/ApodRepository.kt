package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Result
import java.util.*

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource) {
    suspend fun getApod(date: Date): Result<Apod> {
        Result.loading()
        val response = apodDataSource.getApod(date)
        return if(response.isSuccessful) {
            Result.success(response.body())
        } else {
            Result.error(response.errorBody().toString())
        }
    }
}