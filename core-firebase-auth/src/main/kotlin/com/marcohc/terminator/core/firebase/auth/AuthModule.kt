package com.marcohc.terminator.core.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.marcohc.terminator.core.koin.FeatureModule
import com.marcohc.terminator.core.mvi.ext.declareFactoryFragmentRouter
import com.marcohc.terminator.core.mvi.ext.declareFragmentInteractor
import com.marcohc.terminator.core.mvi.ext.fetchOrCreateFromParentScope
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

object AuthModule : FeatureModule {

    override val scopeId = "AuthModule"

    override val module = module {
        single { FirebaseAuth.getInstance() }

        factory { GetGoogleUserUseCase(context = androidApplication()) }

        factory { GetFirebaseUserUseCase(firebaseAuth = get()) }

        declareFactoryFragmentRouter(scopeId) { executor ->
            GoogleSignInRouter(
                executor = executor,
                options = get(named(GOOGLE_SIGN_IN_OPTIONS))
            )
        }

        declareFragmentInteractor(scopeId) {
            GoogleSignInInteractor(
                publisher = scopedGoogleSignInEventPublisher(),
                analytics = GoogleSignInAnalytics(),
                router = get()
            )
        }
    }

    fun Scope.scopedGoogleSignInEventPublisher() = fetchOrCreateFromParentScope(scopeId) { GoogleSignInEventPublisher() }

    const val GOOGLE_SIGN_IN_OPTIONS = "GOOGLE_SIGN_IN_OPTIONS"

}
