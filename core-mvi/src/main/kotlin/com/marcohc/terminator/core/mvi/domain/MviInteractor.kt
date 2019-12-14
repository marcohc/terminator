package com.marcohc.terminator.core.mvi.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Interface for a Interactor to work following MVI
 */
interface MviInteractor<Intention, State> {

    fun subscribeToIntentions(intentions: Observable<Intention>): Disposable

    fun states(): Observable<State>

    fun doOnDestroy(onDestroyFunction: () -> Unit)

}
