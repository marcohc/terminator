package com.marcohc.terminator.core.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.marcohc.terminator.core.koin.CoreModule
import org.koin.dsl.module

object FirestoreModule : CoreModule {

    override val module = module {
        single { FirebaseFirestore.getInstance() }
    }
}
