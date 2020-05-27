package com.marcohc.terminator.core.firebase.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import io.reactivex.Completable

class GoogleSignOutUseCase(
        private val context: Context,
        private val signInOptions: GoogleSignInOptions
) {

    fun execute(): Completable {
        return Completable
            .create { source ->
                GoogleSignIn.getClient(context, signInOptions)
                    .signOut()
                    .addOnCompleteListener {
                        if (!source.isDisposed) {
                            if (it.isSuccessful) {
                                source.onComplete()
                            } else {
                                source.onError(it.exception!!)
                            }
                        }
                    }
            }
    }

}
