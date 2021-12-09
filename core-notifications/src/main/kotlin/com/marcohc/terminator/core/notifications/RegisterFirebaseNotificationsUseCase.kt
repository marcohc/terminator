package com.marcohc.terminator.core.notifications

import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Single
import timber.log.Timber

class RegisterFirebaseNotificationsUseCase {

    fun execute() = Single
        .create<String> { emitter ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            Timber.v("Token: $it")
                            emitter.onSuccess(it)
                        }
                    } else {
                        emitter.onError(requireNotNull(task.exception))
                    }
                }
        }
}
