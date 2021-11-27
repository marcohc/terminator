package com.marcohc.terminator.core.mvi.test

import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor.Companion.MVI_RX_UI_SCHEDULER
import com.marcohc.terminator.core.mvi.domain.MviInteractor
import com.marcohc.terminator.core.mvi.ui.MviView
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

object ViewInteractorTestUtils : KoinComponent {

    fun <Intention, State> bindViewToInteractor(
        view: MviView<Intention, State>,
        interactor: MviInteractor<Intention, State>
    ): Pair<PublishSubject<Intention>, TestObserver<State>> {
        val intentions = PublishSubject.create<Intention>()
        whenever(view.intentions()).thenReturn(intentions)
        interactor.subscribeToIntentions(view.intentions())
        return Pair(intentions, interactor.states().test())
    }

    // First test will throw an exception because Koin is not being started, the following test will just call getKoin()
    fun initInteractorMocks() {
        try {
            getKoin()
        } catch (ignored: IllegalStateException) {
            startKoin {
                modules(
                    listOf(
                        module {
                            single(named(MVI_RX_UI_SCHEDULER)) { Schedulers.trampoline() }
                        }
                    )
                )
            }
        }
    }
}
