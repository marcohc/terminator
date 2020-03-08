package com.marcohc.terminator.core.mvi.ui

import android.app.Activity
import android.content.ComponentCallbacks
import androidx.fragment.app.Fragment

fun ComponentCallbacks.isFinishing(): Boolean {
    return when (this) {
        is Activity -> this.isFinishing
        is Fragment -> this.isRemoving || this.isDetached || !this.isAdded || (this.activity?.run { isFinishing } ?: false)
        else -> false
    }
}
