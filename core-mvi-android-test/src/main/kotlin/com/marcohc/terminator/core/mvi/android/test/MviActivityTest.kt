package com.marcohc.terminator.core.mvi.android.test

import android.content.Context
import android.content.Intent
import androidx.test.espresso.IdlingPolicies
import androidx.test.rule.ActivityTestRule
import com.marcohc.terminator.core.mvi.ui.MviActivity
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.TimeUnit

open class MviActivityTest<Intention, State, Robot>(
    activity: Class<out MviActivity<Intention, State>>,
    private val scopeId: String,
    val robot: Robot,
    val intent: Intent = Intent()
) {

    @get:Rule
    val rule = ActivityTestRule(activity, false, false)

    lateinit var testObserver: TestObserver<Intention>

    private lateinit var stateSubject: BehaviorSubject<State>

    protected fun setState(state: State) {
        stateSubject.onNext(state)
    }

    protected fun executeWithContext(block: Context.() -> Unit) {
        block.invoke(rule.activity)
    }

    protected fun assertIntentionAt(position: Int, predicate: (Intention) -> Boolean) {
        testObserver.assertValueAt(position, predicate)
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

        launchActivity()

        // Set max timeout as 5 seconds
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS)
    }

    private fun launchActivity() {
        rule.launchActivity(intent)
    }

    private fun prepareMocks(): Pair<TestObserver<Intention>, BehaviorSubject<State>> =
        prepareInputAndOutputMocks(
            scopeId = scopeId,
            activityNavigation = true
        )

    inline fun robot(block: Robot.() -> Unit) {
        robot.apply(block)
    }
}
