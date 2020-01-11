package com.joshualorett.nebula.shared

/**
 * A class that indicates the status of a resource from a repository.
 * Created by Joshua on 1/8/2020.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T): Resource<T>()
    data class Error<out T>(val message: String? = null, val data: T? = null) : Resource<T>()
    object Loading : Resource<Nothing>()

    fun successful() : Boolean {
        return this is Success
    }
}