package com.marcohc.terminator.core.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun Context.getColorCompatFromAttribute(attributeId: Int): Int {
    return getColorCompat(getColorFromAttribute(attributeId))
}

fun Context.getColorFromAttribute(attributeId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attributeId, typedValue, true)
    return typedValue.resourceId
}
