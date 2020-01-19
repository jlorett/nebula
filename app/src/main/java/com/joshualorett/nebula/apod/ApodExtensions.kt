package com.joshualorett.nebula.apod

import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodEntity

/**
 * Extensions for [ApodResponse].
 * Created by Joshua on 1/16/2020.
 */

// Convert ApodResponse to an Apod.
fun ApodResponse.toApod(): Apod {
    return Apod(0, this.date, this.title, this.explanation, this.url, this.hdurl, this.copyright)
}

// Convert an ApodEntity to an Apod.
fun ApodEntity.toApod(): Apod {
    return Apod(this.id, this.date, this.title, this.explanation, this.url, this.hdurl, this.copyright)
}

// Convert an Apod to an ApodEntity.
fun Apod.toEntity(): ApodEntity {
    return ApodEntity(
        this.id,
        this.date,
        this.title,
        this.explanation,
        this.url,
        this.hdurl,
        this.copyright
    )
}