package com.marcohc.terminator.core.firebase.auth

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class GoogleSignInEventPublisher internal constructor() {

    private val publisher = PublishSubject.create<GoogleSignInResult>()

    fun observe(): Observable<GoogleSignInResult> = publisher.hide()

    internal fun dispatchResult(event: GoogleSignInResult) = Completable.fromAction { publisher.onNext(event) }

}

sealed class GoogleSignInResult {
    object Success : GoogleSignInResult()
    data class Failure(val throwable: Throwable) : GoogleSignInResult()
}
