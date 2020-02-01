package com.joshualorett.nebula.shared

/**
 * A class that indicates the status of a resource from a repository.
 * Created by Joshua on 1/8/2020.
 */
sealed class Resource<out T, out U> {
    data class Success<out T>(val data: T): Resource<T, Nothing>()
    data class Error<out U>(val data: U) : Resource<Nothing, U>()
    object Loading : Resource<Nothing, Nothing>()

    fun successful() : Boolean {
        return this is Success
    }
}