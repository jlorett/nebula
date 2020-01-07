package com.joshualorett.nebula

/**
 * Delegates service creation to retrofit instance.
 * Created by Joshua on 1/6/2020.
 */
interface RetrofitServiceDelegate {
    fun <T> create(service: Class<T>): T
}