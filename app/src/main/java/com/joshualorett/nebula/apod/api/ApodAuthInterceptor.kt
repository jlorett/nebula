package com.joshualorett.nebula.apod.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches api key before making Apod network requests.
 * Created by Joshua on 3/22/2021.
 */
class ApodAuthInterceptor(private val key: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url
            .newBuilder()
            .addQueryParameter("api_key", key)
            .build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}
