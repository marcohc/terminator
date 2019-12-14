package com.marcohc.terminator.core.mvi.domain

import com.marcohc.terminator.core.mvi.test.ViewInteractorTestUtils.bindViewToInteractor
import com.marcohc.terminator.core.mvi.test.ViewInteractorTestUtils.initInteractorMocks
import com.marcohc.terminator.core.mvi.ui.MviView
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MviBaseInteractorTest {

    @Mock
    private lateinit var view: MviView<MockIntention, MockState>

    private lateinit var interactor: MviBaseInteractor<MockIntention, MockAction, MockState>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        initInteractorMocks()

        interactor = object : MviBaseInteractor<MockIntention, MockAction, MockState>(
            defaultState = MockState(),
            debugMode = true
        ) {
            override fun intentionToAction(): (intention: MockIntention) -> Observable<out MockAction> = { intention ->
                when (intention) {
                    is MockIntention.Initial -> Observable.just(MockAction.IncrementCount())
                    is MockIntention.ButtonClick -> Observable.just(
                        MockAction.IncrementCount(
                            intention.freezeCounter
                        )
                    )
                }
            }

            override fun actionToState(): (currentState: MockState, action: MockAction) -> MockState = { currentState, action ->
                when (action) {
                    is MockAction.IncrementCount -> {
                        if (action.freezeCounter) {
                            currentState
                        } else {
                            currentState.copy(count = currentState.count + 1)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `when view subscribes to interactor default state should be returned`() {
        val (_, testObserver) = bindViewToInteractor(view, interactor)

        testObserver
            .assertNoErrors()
            .assertNotComplete()
            .assertValueCount(1)
            .assertValue { it.count == 0 }
    }

    @Test
    fun `when view subscribes to interactor and sends initial intention a new state should be returned`() {
        val (intentions, testObserver) = bindViewToInteractor(view, interactor)

        intentions.onNext(MockIntention.Initial)

        testObserver
            .assertNoErrors()
            .assertNotComplete()
            .assertValueCount(2)
            .assertValueAt(1) { it.count == 1 }
    }

    @Test
    fun `when view subscribes and state is the same then a new state is not returned`() {
        val (intentions, testObserver) = bindViewToInteractor(view, interactor)

        intentions.onNext(MockIntention.Initial)
        intentions.onNext(MockIntention.ButtonClick())
        intentions.onNext(MockIntention.ButtonClick(true))

        testObserver
            .assertNoErrors()
            .assertNotComplete()
            .assertValueCount(3)
            .assertValueAt(2) { it.count == 2 }
    }

    @Test
    fun `when view subscribes to interactor and send error then receive error`() {
        val (intentions, testObserver) = bindViewToInteractor(view, interactor)
        val throwable = NullPointerException()
        intentions.onNext(MockIntention.Initial)
        intentions.onNext(MockIntention.ButtonClick())

        intentions.onError(throwable)

        testObserver
            .assertError(throwable)
            .assertNotComplete()
    }

    @Test
    fun `when view subscribes to interactor and completes then keep running`() {
        val (intentions, testObserver) = bindViewToInteractor(view, interactor)

        intentions.onComplete()

        testObserver
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `when interactor is cleared then dispose`() {
        val (intentions, testObserver) = bindViewToInteractor(view, interactor)
        intentions.onNext(MockIntention.ButtonClick())

        interactor.destroy()
        intentions.onNext(MockIntention.ButtonClick())

        testObserver
            .assertValueCount(2)
    }

    @Test
    fun `when interactor is cleared then execute onDestroyFunction`() {
        val (intentions, _) = bindViewToInteractor(view, interactor)
        intentions.onNext(MockIntention.ButtonClick())
        val doOnDestroyFunction = mock<() -> Unit>()

        interactor.doOnDestroy(doOnDestroyFunction)
        interactor.destroy()

        verify(doOnDestroyFunction).invoke()
    }

    private sealed class MockIntention {
        object Initial : MockIntention()
        data class ButtonClick(val freezeCounter: Boolean = false) : MockIntention()
    }

    private sealed class MockAction {
        data class IncrementCount(val freezeCounter: Boolean = false) : MockAction()
    }

    data class MockState(
            val count: Int = 0
    )

}
