package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.switchMap
import java.util.*

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource) {
    suspend fun getApod(date: Date): Flow<Resource<Apod>> {
        Resource.Loading
        return apodDataSource.getApod(date).map { response ->
            if(response.isSuccessful) {
                val apod = response.body()
                if(apod == null) {
                    Resource.Error<Apod>("Empty body")
                } else {
                    Resource.Success(apod)
                }
            } else {
                Resource.Error(response.errorBody().toString())
            }
        }
    }
}