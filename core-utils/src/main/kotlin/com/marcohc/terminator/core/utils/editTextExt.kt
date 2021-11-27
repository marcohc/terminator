@file:Suppress("unused")

package com.marcohc.terminator.core.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.setOnTextChanged(function: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // No-op
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No-op
        }

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            function.invoke(text.toString())
        }
    })
}
