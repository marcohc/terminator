package com.marcohc.terminator.core.mvi.domain

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.marcohc.terminator.core.mvi.MviConstants.INTERACTOR_LOG_TAG
import com.marcohc.terminator.core.utils.toDisposableObserver
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.jetbrains.annotations.NotNull
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import timber.log.Timber

/**
 * Base class which orchestrates the subscriptions and logic to manage a [MviInteractor]
 */
abstract class MviBaseInteractor<Intention, Action, State>(
    defaultState: State,
    private val debugMode: Boolean? = false
) : MviInteractor<Intention, State>,
    ViewModel(),
    KoinComponent {

    private val intentionsSubject = PublishSubject.create<Intention>().toSerialized()
    private val stateSubject = BehaviorSubject.createDefault(defaultState).toSerialized()
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val uiScheduler: Scheduler = get(named(MVI_RX_UI_SCHEDULER))
    private val onDestroyFunctionList = mutableListOf<(() -> Unit)>()

    init {
        disposable.add(
            intentionsSubject
                .doOnNext { intention ->
                    Timber.v(
                        INTERACTOR_LOG_TAG,
                        printClassName(intention as Any, debugMode)
                    )
                }
                .flatMap(this.intentionToAction())
                .doOnNext { action ->
                    Timber.v(
                        INTERACTOR_LOG_TAG,
                        printClassName(action as Any, debugMode)
                    )
                }
                .scan(defaultState, this.actionToState())
                .doOnError { Timber.e(it) }
                .observeOn(uiScheduler)
                .subscribeWith(stateSubject.toDisposableObserver())
        )
    }

    abstract fun intentionToAction(): (Intention) -> Observable<out Action>

    abstract fun actionToState(): (State, Action) -> State

    override fun subscribeToIntentions(intentions: Observable<Intention>): Disposable {
        return intentions.subscribeWith(intentionsSubject.toDisposableObserver())
    }

    override fun states(): Observable<State> {
        return stateSubject.hide()
            .distinctUntilChanged()
            .doOnNext { state ->
                Timber.v(
                    INTERACTOR_LOG_TAG,
                    printClassName(state as Any, debugMode)
                )
            }
    }

    override fun doOnDestroy(onDestroyFunction: () -> Unit) {
        onDestroyFunctionList.add(onDestroyFunction)
    }

    override fun onCleared() {
        disposable.dispose()
        onDestroyFunctionList.forEach { it.invoke() }
        super.onCleared()
    }

    // This function is only used to test all scenarios in this class, should not be used
    @VisibleForTesting
    fun destroy() {
        onCleared()
    }

    private fun printClassName(it: Any, debugMode: Boolean? = false): String {
        val javaClass = it.javaClass
        val packageName = javaClass.`package`?.name + "."
        val fullName = javaClass.canonicalName
        val stateContent = if (debugMode == true) " ($it)" else ""
        return fullName?.replace(packageName, "") + stateContent
    }

    companion object {
        const val MVI_RX_UI_SCHEDULER = "MVI_RX_UI_SCHEDULER"
    }
}
