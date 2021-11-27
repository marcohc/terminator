package com.marcohc.terminator.core.firebase.auth

import io.reactivex.Completable

class GoogleSignInAnalytics {

    fun logScreen() = Completable.complete()
    fun logSignInSuccess() = Completable.complete()
    fun logSignInError() = Completable.complete()
}
