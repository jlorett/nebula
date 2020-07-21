package com.joshualorett.nebula.shared

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Extensions for [SubsamplingScaleImageView].
 * Created by Joshua on 7/18/2020.
 */

suspend fun SubsamplingScaleImageView.awaitImageReady() = suspendCancellableCoroutine<Unit> { cont ->
    val listener = object: SubsamplingScaleImageView.OnImageEventListener {
        override fun onImageLoaded() {}

        override fun onReady() {
            cont.resume(Unit)
        }

        override fun onTileLoadError(e: Exception?) {
            cont.cancel()
        }

        override fun onPreviewReleased() {}

        override fun onImageLoadError(e: Exception?) {
            cont.cancel()
        }

        override fun onPreviewLoadError(e: Exception?) {
            cont.cancel()
        }
    }
    cont.invokeOnCancellation { setOnImageEventListener(null) }
    setOnImageEventListener(listener)
}