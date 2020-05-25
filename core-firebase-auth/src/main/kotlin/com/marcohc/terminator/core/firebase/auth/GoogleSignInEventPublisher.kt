package com.marcohc.terminator.core.firebase.auth

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class GoogleSignInEventPublisher {

    private val publisher = PublishSubject.create<LogInEvent>()

    fun triggerEvent(event: LogInEvent) = Completable.fromAction { publisher.onNext(event) }

    fun observe(): Observable<LogInEvent> = publisher.hide()

}

sealed class LogInEvent {
    object Success : LogInEvent()
    data class Failure(val throwable: Throwable) : LogInEvent()
}
