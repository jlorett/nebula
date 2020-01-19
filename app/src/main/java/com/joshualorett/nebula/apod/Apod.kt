package com.joshualorett.nebula.apod

/**
 * Astronomy Picture of the Day from [Nasa's Astronomy Picture of the Day](https://apod.nasa.gov/apod/astropix.html).
 * Created by Joshua on 1/4/2020.
 */
data class Apod(val id: Long, val date: String, val title: String, val explanation: String,
                val url: String, val hdurl: String? = null, val copyright: String? = null)