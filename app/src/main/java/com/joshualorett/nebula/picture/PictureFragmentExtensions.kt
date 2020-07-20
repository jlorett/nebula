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