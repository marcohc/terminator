package com.marcohc.terminator.core.utils

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator

//region is visibility X region
fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE
//endregion

fun View.setVisible(animate: Boolean = false) {
    if (animate) {
        animation = AlphaAnimation(0f, 1f)
            .apply {
                interpolator = DecelerateInterpolator()
                duration = context
                    .resources
                    .getInteger(android.R.integer.config_shortAnimTime)
                    .toLong()
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                        // No-op
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        visibility = View.VISIBLE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        // No-op
                    }
                })
            }
    } else {
        visibility = View.VISIBLE
    }
}

fun View.setInvisible(animate: Boolean = false) {
    removeVisibility(View.INVISIBLE, animate)
}

fun View.setGone(animate: Boolean = false) {
    removeVisibility(View.GONE, animate)
}

fun View.setVisibleEitherGone(visible: Boolean, animate: Boolean = false) {
    if (visible) {
        setVisible(animate)
    } else {
        setGone(animate)
    }
}

fun View.setVisibleEitherInvisible(visible: Boolean, animate: Boolean = false) {
    if (visible) setVisible(animate) else setInvisible(animate)
}

private fun View.removeVisibility(visibilityParam: Int, animate: Boolean = false) {
    if (animate) {
        animation = AlphaAnimation(1f, 0f)
            .apply {
                interpolator = AccelerateInterpolator()
                duration = context
                    .resources
                    .getInteger(android.R.integer.config_shortAnimTime)
                    .toLong()
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                        // No-op
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        visibility = visibilityParam
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        visibility = View.VISIBLE
                    }
                })
            }
    } else {
        visibility = visibilityParam
    }
}
