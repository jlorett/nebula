package com.joshualorett.nebula.today

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

/**
 * Extensions for [TodayPhotoFragment].
 * Created by Joshua on 7/15/2020.
 */

fun TodayPhotoFragment.prepareApodAnimation() {
    binding.todayDate.alpha = 0F
    binding.todayTitle.alpha = 0F
    binding.todayDescription.alpha = 0F
    binding.todayCopyright.alpha = 0F
}

fun TodayPhotoFragment.animateApod() {
    val interpolator = LinearOutSlowInInterpolator()
    val duration = 300L
    binding.todayDate.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    binding.todayTitle.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    binding.todayDescription.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    binding.todayCopyright.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
}

fun TodayPhotoFragment.prepareErrorAnimation() {
    binding.todayTitle.alpha = 0F
    binding.todayDescription.alpha = 0F
}

fun TodayPhotoFragment.animateError() {
    val interpolator = LinearOutSlowInInterpolator()
    val duration = 300L
    binding.todayTitle.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
    binding.todayDescription.animate()
        .alpha(1F)
        .setInterpolator(interpolator)
        .setDuration(duration)
}
