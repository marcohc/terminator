package com.marcohc.terminator.core.notifications

import com.marcohc.terminator.core.koin.CoreModule
import org.koin.dsl.module

object NotificationsModule : CoreModule {

    override val module = module {

        single { NotificationsRepository() }

        factory { RegisterFirebaseNotificationsUseCase() }
    }
}
