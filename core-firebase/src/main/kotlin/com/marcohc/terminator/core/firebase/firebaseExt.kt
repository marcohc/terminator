package com.marcohc.terminator.core.firebase

import com.google.android.gms.tasks.Task

fun <TResult> Task<TResult>.onSuccess(function: (TResult) -> Unit): Task<TResult> {
    return addOnSuccessListener({ Thread(it).run() }, { tResult -> function.invoke(tResult) })
}

fun <TResult> Task<TResult>.onError(function: (Exception) -> Unit): Task<TResult> {
    return addOnFailureListener({ Thread(it).run() }, { exception -> function.invoke(exception) })
}
