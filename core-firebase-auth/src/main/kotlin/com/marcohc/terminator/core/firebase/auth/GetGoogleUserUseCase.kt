package com.marcohc.terminator.core.firebase.auth

import android.content.Context
import com.gojuno.koptional.Optional
import com.google.android.gms.auth.api.signin.GoogleSignIn
import io.reactivex.Single

class GetGoogleUserUseCase(private val context: Context) {

    fun execute() = Single.fromCallable {
        GoogleSignIn.getLastSignedInAccount(context) ?: throw GoogleUserNotLoggedInException()
    }

    fun executeOptional() = Single.fromCallable {
        Optional.toOptional(GoogleSignIn.getLastSignedInAccount(context))
    }

}
