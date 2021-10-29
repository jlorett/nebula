package com.joshualorett.nebula.ui.picture

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.joshualorett.nebula.databinding.FragmentPictureBinding
import com.joshualorett.nebula.shared.awaitImageReady

/**
 * Extensions for [PictureFragment].
 * Created by Joshua on 7/20/2020.
 */

fun PictureFragment.preparePictureAnimation(binding: FragmentPictureBinding) {
    binding.apodPicture.alpha = 0F
}

suspend fun PictureFragment.animatePicture(binding: FragmentPictureBinding) {
    binding.apodPicture.run {
        awaitImageReady()
        animate()
            .alpha(1F)
            .setInterpolator(LinearOutSlowInInterpolator())
            .setDuration(300)
    }
}

fun PictureFragment.prepareErrorAnimation(binding: FragmentPictureBinding) {
    binding.pictureError.alpha = 0F
}

fun PictureFragment.animateError(binding: FragmentPictureBinding) {
    val interpolator = LinearOutSlowInInterpolator()
    val duration = 300L
    binding.pictureError.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
}
