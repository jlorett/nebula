package com.joshualorett.nebula.picture

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.joshualorett.nebula.shared.awaitImageReady
import kotlinx.android.synthetic.main.fragment_picture.*

/**
 * Extensions for [PictureFragment].
 * Created by Joshua on 7/20/2020.
 */

fun PictureFragment.preparePictureAnimation() {
    apodPicture.alpha = 0F
}

suspend fun PictureFragment.animatePicture() {
    apodPicture.run {
        awaitImageReady()
        animate()
            .alpha(1F)
            .setInterpolator(LinearOutSlowInInterpolator())
            .setDuration(300)
    }
}

fun PictureFragment.prepareErrorAnimation() {
    pictureError.alpha = 0F
}

fun PictureFragment.animateError() {
    val interpolator = LinearOutSlowInInterpolator()
    val duration = 300L
    pictureError.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
}