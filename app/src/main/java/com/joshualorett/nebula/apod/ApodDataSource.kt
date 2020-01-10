package com.joshualorett.nebula.apod

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.*

/**
 * The data source for [Nasa's Astronomy Picture of the Day API](https://api.nasa.gov/).
 * Created by Joshua on 1/5/2020.
 */
interface ApodDataSource {
    suspend fun getApod(date: Date): Flow<Response<Apod>>
}