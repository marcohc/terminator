package com.marcohc.terminator.core.firebase.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.Single

class GetGoogleUserUseCase(private val context: Context) {

    fun execute(): Single<GoogleSignInAccount> = Single
        .fromCallable {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || account.idToken == null || account.id == null || account.email == null) {
                throw GoogleUserNotLoggedInException()
            } else {
                account
            }
        }

}
