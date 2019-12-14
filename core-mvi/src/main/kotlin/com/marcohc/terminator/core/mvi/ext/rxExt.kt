package com.marcohc.terminator.core.mvi.ext

import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.Subject

fun <T> Subject<T>.toDisposableObserver(): DisposableObserver<T> =
    DisposableSubject(this)

private class DisposableSubject<T>(
    private val subject: Subject<T>
) : DisposableObserver<T>() {

    override fun onNext(next: T) {
        subject.onNext(next)
    }

    override fun onComplete() {
        subject.onComplete()
    }

    override fun onError(e: Throwable) {
        subject.onError(e)
    }
}
