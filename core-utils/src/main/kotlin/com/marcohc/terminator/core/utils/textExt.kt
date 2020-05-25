package com.marcohc.terminator.core.utils

import android.widget.TextView

fun TextView.setTextColorCompat(resourceId: Int) {
    setTextColor(context.getColorCompat(resourceId))
}
