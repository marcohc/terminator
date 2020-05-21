package com.marcohc.terminator.core.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Single

class GetFirebaseUserUseCase(private val firebaseAuth: FirebaseAuth) {

    fun execute() = Single.fromCallable { firebaseAuth.currentUser ?: throw FirebaseUserNotLoggedInException() }

}
