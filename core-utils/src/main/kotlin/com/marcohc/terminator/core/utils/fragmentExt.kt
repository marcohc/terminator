package com.marcohc.terminator.core.utils

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackbar(resourceId: Int) {
    activity?.run {
        Snackbar.make(window.decorView.findViewById(android.R.id.content), resourceId, Snackbar.LENGTH_LONG).show()
    }
}

fun Fragment.showSnackbar(text: String) {
    activity?.run {
        Snackbar.make(window.decorView.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
    }
}
