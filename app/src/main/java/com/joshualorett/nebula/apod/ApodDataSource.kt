package com.joshualorett.nebula.apod

import retrofit2.Response
import java.util.*

/**
 * The data source for an apod.
 * Created by Joshua on 1/5/2020.
 */
interface ApodDataSource {
    suspend fun getApod(date: Date): Response<Apod>
}