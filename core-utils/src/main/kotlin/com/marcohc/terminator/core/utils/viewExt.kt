package com.marcohc.terminator.core.utils

import android.view.View

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun View.setGone() {
    visibility = View.GONE
}

fun View.setVisibleEitherGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setVisibleEitherInvisble(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}
