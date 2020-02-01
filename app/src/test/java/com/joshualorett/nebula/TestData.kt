package com.joshualorett.nebula

import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.api.ApodResponse
import com.joshualorett.nebula.apod.database.ApodEntity

/**
 * Test data.
 * Created by Joshua on 1/23/2020.
 */
object TestData {
    val apodEntity = ApodEntity(
        1L, "2000-01-01", "apod", "testing",
        "image", "https://example.com","https://example.com/hd",
        "tester"
    )

    val apodResponse = ApodResponse(
        0, "2000-01-01", "apod", "testing",
        "image", "v1", "https://example.com",
        "https://example.com/hd"
    )

    val apod = Apod(
        1, "2000-01-01", "apod", "testing",
        "image", "https://example.com", "https://example.com/hd", "tester"
    )

    val videoApod = Apod(
        1, "2000-01-01", "apod", "testing",
        "video", "https://example.com", null, null
    )
}