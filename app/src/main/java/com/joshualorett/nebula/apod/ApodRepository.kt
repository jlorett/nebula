package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Resource
import java.util.*

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource) {
    suspend fun getApod(date: Date): Resource<Apod> {
        Resource.Loading
        val response = apodDataSource.getApod(date)
        return if(response.isSuccessful) {
            val apod = response.body()
            if(apod == null) {
                Resource.Error("Empty body")
            } else {
                Resource.Success(apod)
            }
        } else {
            Resource.Error(response.errorBody().toString())
        }
    }
}