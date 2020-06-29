package com.marcohc.terminator.core.notifications

import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import timber.log.Timber

class RegisterFirebaseNotificationsUseCase {

    fun execute() = Single
        .create<String> { emitter ->
            FirebaseInstanceId.getInstance()
                .instanceId
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.token?.let {
                            Timber.v("Token: $it")
                            emitter.onSuccess(it)
                        }
                    } else {
                        emitter.onError(requireNotNull(task.exception))
                    }
                }
        }
}
