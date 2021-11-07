package com.marcohc.terminator.core.mvi.test

import androidx.annotation.VisibleForTesting
import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.test.ViewInteractorTestUtils.bindViewToInteractor
import com.marcohc.terminator.core.mvi.ui.MviView
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@VisibleForTesting
abstract class MviInteractorTest<Intention, Action, State> {

    @Mock
    lateinit var view: MviView<Intention, State>

    abstract fun provideInteractor(): MviBaseInteractor<Intention, Action, State>

    private lateinit var interactor: MviBaseInteractor<Intention, Action, State>

    lateinit var testObserver: TestObserver<State>

    private lateinit var intentions: PublishSubject<Intention>

    protected fun assertStateAt(position: Int, predicate: (State) -> Boolean) {
        testObserver.assertValueAt(position, predicate)
    }

    protected inline fun <reified T : State> assertTypedStateAt(position: Int, crossinline predicate: T.() -> Boolean) {
        testObserver.assertValueAt(position) {
            require(it is T)
            predicate(it)
        }
    }

    protected fun State.assertStateAt(position: Int) {
        testObserver.assertValueAt(position, this)
    }

    protected fun assertValueCount(count: Int) {
        testObserver.assertValueCount(count)
    }

    @Throws(IllegalStateException::class)
    protected fun assertException(state: State, action: Action) {
        interactor.actionToState().invoke(state, action)
    }

    @Before
    fun init() {
        MockitoAnnotations.openMocks(this)
        ViewInteractorTestUtils.initInteractorMocks()
        interactor = provideInteractor()

        val (subject, observer) = bindViewToInteractor(view, interactor)
        testObserver = observer
        intentions = subject
    }

    fun sendIntention(intention: Intention) {
        intentions.onNext(intention)
    }
}
