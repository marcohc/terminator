package com.marcohc.terminator.core.utils

import android.app.Activity
import com.google.android.material.snackbar.Snackbar

fun Activity.showSnackbar(resourceId: Int) {
    Snackbar.make(window.decorView.findViewById(android.R.id.content), resourceId, Snackbar.LENGTH_LONG).show()
}

fun Activity.showSnackbar(text: String) {
    Snackbar.make(window.decorView.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
}
