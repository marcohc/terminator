package com.marcohc.terminator.core.firebase.functions

import com.google.firebase.functions.FirebaseFunctions
import com.marcohc.terminator.core.koin.CoreModule
import org.koin.dsl.module

object FunctionsModule : CoreModule {

    override val module = module {
        single { FirebaseFunctions.getInstance() }
    }

}
