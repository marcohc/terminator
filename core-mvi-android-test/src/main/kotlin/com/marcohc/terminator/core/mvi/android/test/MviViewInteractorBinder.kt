package com.marcohc.terminator.core.mvi.android.test

import com.marcohc.terminator.core.mvi.domain.MviInteractor
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutorImpl
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutorImpl
import com.marcohc.terminator.core.utils.toDisposableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun <Intention, State> prepareInputAndOutputMocks(
        scopeId: String,
        activityNavigation: Boolean,
        modules: List<Module> = emptyList()
): Pair<TestObserver<Intention>, BehaviorSubject<State>> {
    val (intentionsObserver, stateSubject) = mockMviInteractor<Intention, State>(
        scopeId = scopeId,
        activityNavigation = activityNavigation,
        modules = modules
    )
    return Pair(intentionsObserver, stateSubject)
}

/**
 * Loads a Koin module and returns a publish subject for mocking the states
 */
private fun <Intention, State> mockMviInteractor(
        scopeId: String,
        activityNavigation: Boolean,
        modules: List<Module> = emptyList()
): Pair<TestObserver<Intention>, BehaviorSubject<State>> {

    val stateSubject = BehaviorSubject.create<State>()
    val intentionsSubject = PublishSubject.create<Intention>()

    val koinModule = module(override = true) {
        factory(named(scopeId)) {
            createMockInteractor<Intention, State>(intentionsSubject, stateSubject)
        }

        scope(named(scopeId)) {
            if (activityNavigation) {
                scoped<ActivityNavigationExecutor>(named(scopeId)) { ActivityNavigationExecutorImpl() }
            } else {
                scoped<FragmentNavigationExecutor>(named(scopeId)) { FragmentNavigationExecutorImpl() }
            }
        }
    }

    modules.forEach { loadKoinModules(it) }
    loadKoinModules(koinModule)
    return Pair(intentionsSubject.test(), stateSubject)
}

private fun <Intention, State> createMockInteractor(
        intentionsSubject: PublishSubject<Intention>,
        stateSubject: BehaviorSubject<State>
): MviInteractor<Intention, State> {
    return object : MviInteractor<Intention, State> {

        override fun subscribeToIntentions(intentions: Observable<Intention>): Disposable {
            return intentions.subscribeWith(intentionsSubject.toDisposableObserver())
        }

        override fun states(): Observable<State> {
            return stateSubject.observeOn(AndroidSchedulers.mainThread())
        }

        override fun doOnDestroy(onDestroyFunction: () -> Unit) {
            // No-op
        }
    }
}

