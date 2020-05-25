package com.marcohc.terminator.core.firebase.auth

import com.marcohc.terminator.core.koin.FeatureModule
import com.marcohc.terminator.core.mvi.ext.declareFactoryFragmentRouter
import com.marcohc.terminator.core.mvi.ext.declareFragmentInteractor
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object GoogleSignInModule : FeatureModule {

    override val scopeId: String = "GoogleSignInModule"

    override val module: Module = module {

        single { GoogleSignInEventPublisher() }

        declareFactoryFragmentRouter(scopeId) { executor ->
            GoogleSignInRouter(
                navigationExecutor = executor,
                builder = get(named(GOOGLE_SIGN_IN_BUILDER))
            )
        }

        declareFragmentInteractor(scopeId) {
            GoogleSignInInteractor(
                publisher = get(),
                analytics = GoogleSignInAnalytics(),
                router = get()
            )
        }
    }

    const val GOOGLE_SIGN_IN_BUILDER = "GOOGLE_SIGN_IN_BUILDER"
}
