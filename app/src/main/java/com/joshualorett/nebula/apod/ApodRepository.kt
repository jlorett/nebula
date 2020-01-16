package com.joshualorett.nebula.apod

import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.flow.*
import java.time.LocalDate

/**
 * Single point of access to fetch an [Apod] from the ui.
 * Created by Joshua on 1/8/2020.
 */
class ApodRepository(private val apodDataSource: ApodDataSource) {
    // The first APOD was 1995-06-16, month is 0 based.
    private val earliestDate: LocalDate = LocalDate.of(1995, 6, 16)

    suspend fun getApod(date: LocalDate): Flow<Resource<Apod>> {
        if (date.isBefore(earliestDate)) {
            return flowOf(Resource.Error("Date can't be before 1995-06-16"))
        }

        return apodDataSource.getApod(date).map { response ->
            if(response.isSuccessful) {
                val apod = response.body()
                if(apod == null) {
                    Resource.Error("Empty body")
                } else {
                    Resource.Success(apod)
                }
            } else {
                Resource.Error("Error getting apod with status ${response.code()}.")
            }
        }
    }
}