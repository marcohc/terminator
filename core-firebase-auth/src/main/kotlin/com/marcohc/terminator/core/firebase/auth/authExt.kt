package com.marcohc.terminator.core.firebase.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.marcohc.terminator.core.firebase.onError
import com.marcohc.terminator.core.firebase.onSuccess
import io.reactivex.Completable
import timber.log.Timber

fun FirebaseAuth.authenticateWithGoogle(token: String): Completable {
    return Completable
        .create { emitter ->
            signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                .onSuccess { emitter.onComplete() }
                .onError { emitter.onError(it) }
        }
        .doOnComplete { Timber.v("FirebaseAuth --> authenticateWithGoogle success") }
        .doOnError { Timber.e(it, "FirebaseAuth --> authenticateWithGoogle error") }
}

fun FirebaseAuth.authenticateAnonymously(): Completable {
    return Completable
        .create { emitter ->
            signInAnonymously()
                .onSuccess { emitter.onComplete() }
                .onError { emitter.onError(it) }
        }
        .doOnComplete { Timber.v("FirebaseAuth --> authenticateAnonymously success") }
        .doOnError { Timber.e(it, "FirebaseAuth --> authenticateAnonymously error") }
}

fun FirebaseUser.linkAnonymousAccount(token: String): Completable {
    return Completable
        .create { emitter ->
            linkWithCredential(GoogleAuthProvider.getCredential(token, null))
                .onSuccess { emitter.onComplete() }
                .onError { emitter.onError(it) }
        }
        .doOnComplete { Timber.v("FirebaseAuth --> authenticateAnonymously success") }
        .doOnError { Timber.e(it, "FirebaseAuth --> authenticateAnonymously error") }
}

fun FirebaseAuth.logOut(context: Context): Completable {
    return Completable
        .create { emitter ->
            signOut()
            GoogleSignIn
                .getClient(
                    context,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
                )
                .signOut()
                .addOnCompleteListener {
                    if (!emitter.isDisposed) {
                        if (it.isSuccessful) {
                            emitter.onComplete()
                        } else {
                            emitter.onError(it.exception!!)
                        }
                    }
                }
        }
        .doOnComplete { Timber.v("FirebaseAuth --> logOut success") }
        .doOnError { Timber.e(it, "FirebaseAuth --> logOut error") }
}
