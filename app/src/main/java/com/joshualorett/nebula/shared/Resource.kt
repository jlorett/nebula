package com.joshualorett.nebula.shared

import com.joshualorett.nebula.shared.Status.*

/**
 * A data class that holds a value with its current [Status].
 * @param status the current status of the resource.
 * @param data the data this resource holds
 * @param message the error message.
 * Created by Joshua on 1/8/2020.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Success, data, null)
        }

        fun <T> error(msg: String, data: T? = null): Resource<T> {
            return Resource(Error, data, msg)
        }

        fun loading(): Resource<Nothing> {
            return Resource(Loading, null, null)
        }
    }
}