package com.joshualorett.nebula.picture

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * A [BitmapTransformation] that displays an image fullscreen, keeping the aspect ratio. This should
 * be used in conjunction with a scrollview as the dimensions can exceed the screen size.
 * Created by Joshua on 1/23/2020.
 */
class FullScreen(private val screenWidth: Int, private val screenHeight: Int): BitmapTransformation() {
    private val id = "com.joshualorett.transformations.FullScreen"
    private val idBytes: ByteArray = id.toByteArray(Charset.forName("UTF-8"))

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val isPortrait = screenHeight > screenWidth
        val ratio: Float = if (isPortrait) outWidth/outHeight.toFloat() else outHeight/outWidth.toFloat()
        return if (isPortrait) {
            val newWidth = (ratio * outHeight).toInt()
            Bitmap.createScaledBitmap(toTransform, newWidth, outHeight, true)
        } else {
            val newHeight = (ratio * outWidth).toInt()
            Bitmap.createScaledBitmap(toTransform, outWidth, newHeight, true)
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is FullScreen
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)
    }
}