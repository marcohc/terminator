package com.marcohc.terminator.core.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat

fun ImageView.setTintColor(color: Int) {
    setColorFilter(ContextCompat.getColor(context, color), android.graphics.PorterDuff.Mode.SRC_IN)
}
