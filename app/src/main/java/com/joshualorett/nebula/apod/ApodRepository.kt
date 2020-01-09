package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Resource
import java.util.*

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource) {
    suspend fun getApod(date: Date): Resource<Apod> {
        Resource.loading()
        val response = apodDataSource.getApod(date)
        return if(response.isSuccessful) {
            Resource.success(response.body())
        } else {
            Resource.error(response.errorBody().toString())
        }
    }
}