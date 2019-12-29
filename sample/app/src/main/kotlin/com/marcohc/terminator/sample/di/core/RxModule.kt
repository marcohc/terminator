package com.marcohc.terminator.sample.di.core

import com.marcohc.terminator.core.koin.CoreModule
import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor.Companion.MVI_RX_UI_SCHEDULER
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.qualifier.named
import org.koin.dsl.module

object RxModule : CoreModule {

    override val module = module {
        single { Schedulers.io() }
        single(named(MVI_RX_UI_SCHEDULER)) { AndroidSchedulers.mainThread() }
    }

}
