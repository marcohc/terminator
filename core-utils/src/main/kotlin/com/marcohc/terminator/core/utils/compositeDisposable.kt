@file:Suppress("unused")

package com.marcohc.terminator.core.utils

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun CompositeDisposable.executeFunctionOnIo(function: () -> Unit) {
    add(
        Completable.fromAction { function.invoke() }
            .subscribeOn(Schedulers.io())
            .subscribe()
    )
}

fun CompositeDisposable.executeCompletableOnIo(function: () -> Completable) {
    add(
        function.invoke()
            .subscribeOn(Schedulers.io())
            .subscribe()
    )
}

fun CompositeDisposable.executeCompletable(function: () -> Completable) {
    add(
        function.invoke()
            .subscribe()
    )
}
