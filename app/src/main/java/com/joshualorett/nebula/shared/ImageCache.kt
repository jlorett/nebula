package com.joshualorett.nebula.shared

import android.content.Context
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Image cache utilities.
 * Created by Joshua on 1/25/2020.
 */
interface ImageCache {
    fun attachApplicationContext(appContext: Context)
    fun detachApplicationContext()
    suspend fun cleanCache()
}

class GlideImageCache(private val dispatcher: CoroutineDispatcher): ImageCache {
    private var appContext: Context? = null

    override fun attachApplicationContext(appContext: Context) {
        this.appContext = appContext.applicationContext
    }

    override fun detachApplicationContext() {
        this.appContext = null
    }

    override suspend fun cleanCache() {
        withContext(dispatcher) {
            appContext?.let {
                Glide.get(it).clearDiskCache()
            }
        }
    }
}