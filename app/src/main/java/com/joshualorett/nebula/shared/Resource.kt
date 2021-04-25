package com.joshualorett.nebula.shared

/**
 * A class that indicates the status of a resource from a repository.
 * Created by Joshua on 1/8/2020.
 */
sealed class Resource<out T, out U> {
    data class Success<out T>(val data: T) : Resource<T, Nothing>()
    data class Error<out U>(val error: U) : Resource<Nothing, U>()
    object Loading : Resource<Nothing, Nothing>()
    fun successful(): Boolean {
        return this is Success
    }
}

/**
 * Return data T if resource is successful otherwise null.
 */
val <T, U> Resource<T, U>.data: T?
    get() = (this as? Resource.Success)?.data

/**
 * Return error U if resource is error otherwise null.
 */
val <T, U> Resource<T, U>.error: U?
    get() = (this as? Resource.Error)?.error
