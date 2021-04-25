package com.joshualorett.nebula.shared

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Cache image urls.
 * Created by Joshua on 1/25/2020.
 */
interface ImageCache {
    fun attachApplicationContext(appContext: Context)
    fun detachApplicationContext()
    suspend fun cache(url: String): Boolean
    suspend fun clear()
}

class GlideImageCache @Inject constructor(private val dispatcher: CoroutineDispatcher) :
    ImageCache {
    private var appContext: Context? = null

    override fun attachApplicationContext(appContext: Context) {
        this.appContext = appContext.applicationContext
    }

    override fun detachApplicationContext() {
        this.appContext = null
    }

    override suspend fun clear() {
        withContext(dispatcher) {
            appContext?.let {
                Glide.get(it).clearDiskCache()
            }
        }
    }

    override suspend fun cache(url: String): Boolean = withContext(dispatcher) {
        val ctx = appContext ?: return@withContext false
        val imageFuture = Glide.with(ctx)
            .downloadOnly()
            .load(GlideUrl(url))
            .submit()
        try {
            val cacheResult = imageFuture.get()
            cacheResult != null
        } catch (e: Exception) {
            false
        }
    }
}
