package com.marcohc.terminator.core.firebase.auth

import io.reactivex.Single

class IsGoogleUserSignedInUseCase(private val getGoogleUserUseCase: GetGoogleUserUseCase) {

    fun execute(): Single<Boolean> {
        return getGoogleUserUseCase.execute()
            .map { true }
            .onErrorReturn { false }
    }
}
