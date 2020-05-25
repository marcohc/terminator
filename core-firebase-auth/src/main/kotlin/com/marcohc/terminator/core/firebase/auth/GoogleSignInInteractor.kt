package com.marcohc.terminator.core.firebase.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.firebase.auth.GoogleSignInIntention.ActivityResult
import com.marcohc.terminator.core.firebase.auth.GoogleSignInIntention.Initial
import com.marcohc.terminator.core.firebase.auth.GoogleSignInRouter.Companion.REQUEST_CODE_SIGN_IN
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber

sealed class GoogleSignInIntention {
    object Initial : GoogleSignInIntention()
    data class ActivityResult(
            val requestCode: Int,
            val intent: Intent?
    ) : GoogleSignInIntention()
}

internal class GoogleSignInInteractor(
        private val publisher: GoogleSignInEventPublisher,
        private val analytics: GoogleSignInAnalytics,
        private val router: GoogleSignInRouter
) : MviBaseInteractor<GoogleSignInIntention, GoogleSignInAction, GoogleSignInState>(defaultState = GoogleSignInState) {

    override fun intentionToAction(): (GoogleSignInIntention) -> Observable<out GoogleSignInAction> = { intention ->
        when (intention) {
            is Initial -> initial().toObservable()
            is ActivityResult -> activityResult(intention.requestCode, intention.intent).toObservable()
        }
    }

    private fun initial() = analytics.logScreen()
        .andThen(router.showSignInDialog())

    private fun activityResult(requestCode: Int, intent: Intent?): Completable {
        val exception = when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                try {
                    if (intent != null) GoogleSignIn.getSignedInAccountFromIntent(intent)?.exception as ApiException? else null
                } catch (exception: Exception) {
                    Timber.e(exception, "GoogleSignIn.getSignedInAccountFromIntent(data).exception as ApiException?")
                    exception
                }
            }
            else -> IllegalStateException("This code does not belong here")
        }
        // Sign in success
        return if (exception == null) {
            Completable
                .fromAction { analytics.logSignInSuccess() }
                .andThen(publisher.dispatchResult(GoogleSignInResult.Success))
                .andThen(router.dismiss())
        }
        // Sign in error
        else {
            analytics.logSignInError()
                .andThen(publisher.dispatchResult(GoogleSignInResult.Failure(exception)))
                .andThen(router.dismiss())
        }
    }

    override fun actionToState(): (GoogleSignInState, GoogleSignInAction) -> GoogleSignInState = { _, _ ->
        GoogleSignInState
    }
}

object GoogleSignInAction

object GoogleSignInState
