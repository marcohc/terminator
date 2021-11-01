package com.marcohc.terminator.core.mvi.android.test

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.test.espresso.IdlingPolicies
import androidx.test.rule.ActivityTestRule
import com.marcohc.terminator.core.mvi.ui.MviBottomSheetDialogFragment
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.TimeUnit

@VisibleForTesting
abstract class MviBottomSheetDialogFragmentTest<Intention, State, Robot>(
        private val fragment: MviBottomSheetDialogFragment<Intention, State>,
        private val scopeId: String,
        val robot: Robot
) {

    @get:Rule
    val rule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)

    lateinit var testObserver: TestObserver<Intention>

    private lateinit var stateSubject: BehaviorSubject<State>

    protected fun setState(state: State) {
        stateSubject.onNext(state)
    }

    protected fun executeWithContext(block: Context.() -> Unit) {
        block.invoke(rule.activity)
    }

    protected inline fun <reified T : Intention> assertTypedIntentionAt(position: Int, crossinline predicate: T.() -> Boolean) {
        testObserver.assertValueAt(position) {
            require(it is T)
            predicate(it)
        }
    }

    protected fun Intention.assertFirstIntention() {
        testObserver
            .awaitCount(1)
            .assertValue(this)
    }

    protected fun Intention.assertIntentionAt(position: Int) {
        testObserver
            .awaitCount(position + 1)
            .assertValueAt(position, this)
    }

    @Before
    fun init() {
        val (observer, subject) = prepareMocks()
        testObserver = observer
        stateSubject = subject

        rule.activity.setFragment(fragment)

        // Set max timeout as 5 seconds
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS)
    }

    private fun prepareMocks(): Pair<TestObserver<Intention>, BehaviorSubject<State>> = prepareInputAndOutputMocks(
        scopeId = scopeId,
        activityNavigation = false
    )

    inline fun robot(block: Robot.() -> Unit) {
        robot.apply(block)
    }
}
