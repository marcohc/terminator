package com.marcohc.terminator.core.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.marcohc.terminator.core.koin.CoreModule
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

object AuthModule : CoreModule {

    override val module = module {
        single { FirebaseAuth.getInstance() }

        factory { GetGoogleUserUseCase(context = androidApplication()) }

        factory { GetFirebaseUserUseCase(firebaseAuth = get()) }
    }

}
