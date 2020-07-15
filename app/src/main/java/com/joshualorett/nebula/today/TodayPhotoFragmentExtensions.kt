package com.joshualorett.nebula.today

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import kotlinx.android.synthetic.main.fragment_today_photo.*

/**
 * Extensions for [TodayPhotoFragment].
 * Created by Joshua on 7/15/2020.
 */

fun TodayPhotoFragment.prepareApodAnimation() {
    todayDate.alpha = 0F
    todayTitle.alpha = 0F
    todayDescription.alpha = 0F
    todayCopyright.alpha = 0F
}

fun TodayPhotoFragment.animateApod() {
    val interpolator = LinearOutSlowInInterpolator()
    val duration = 300L
    todayDate.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    todayTitle.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    todayDescription.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    todayCopyright.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
}

fun TodayPhotoFragment.prepareErrorAnimation() {
    todayTitle.alpha = 0F
    todayDescription.alpha = 0F
}

fun TodayPhotoFragment.animateError() {
    val interpolator = LinearOutSlowInInterpolator()
    val duration = 300L
    todayTitle.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    todayDescription.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
}