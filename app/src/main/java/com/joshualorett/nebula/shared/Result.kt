package com.joshualorett.nebula.shared

import com.joshualorett.nebula.shared.Status.*

/**
 * A data class that holds a value with its current [Status].
 * @param <T>
 * Created by Joshua on 1/8/2020.
 */
data class Result<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Result<T> {
            return Result(Success, data, null)
        }

        fun <T> error(msg: String, data: T? = null): Result<T> {
            return Result(Error, data, msg)
        }

        fun loading(): Result<Nothing> {
            return Result(Loading, null, null)
        }
    }
}