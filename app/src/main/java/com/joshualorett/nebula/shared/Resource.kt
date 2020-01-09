package com.joshualorett.nebula.shared

import com.joshualorett.nebula.shared.Status.*

/**
 * A data class that holds a value with its current [Status].
 * @param status the current status of the resource.
 * Created by Joshua on 1/8/2020.
 */
sealed class Resource<out T>(val status: Status) {
    data class Success<out T>(val data: T): Resource<T>(Success)
    data class Error<out T>(val message: String, val data: T? = null) : Resource<T>(Error)
    object Loading : Resource<Nothing>(Status.Loading)
}