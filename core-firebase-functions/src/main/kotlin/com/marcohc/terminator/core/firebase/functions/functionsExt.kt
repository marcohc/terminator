package com.marcohc.terminator.core.firebase.functions

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.marcohc.terminator.core.firebase.onError
import com.marcohc.terminator.core.firebase.onSuccess
import io.reactivex.Single

fun FirebaseFunctions.call(functionName: String, data: Any): Single<HttpsCallableResult> {
    return Single.create { emitter ->
        getHttpsCallable(functionName)
            .call(data)
            .onSuccess { emitter.onSuccess(it) }
            .onError { emitter.onError(it) }
    }
}
