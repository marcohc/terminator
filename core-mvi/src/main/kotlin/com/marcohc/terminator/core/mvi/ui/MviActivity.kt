package com.marcohc.terminator.core.mvi.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.mvi.domain.MviInteractor
import com.marcohc.terminator.core.mvi.ui.navigation.BackPressedObserver
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 *
 * This class is the base class for an Activity which wants to implement MVI architecture.
 *
 * Just extend your Activity and fill out the specific variables for it.
 *
 * Then to trigger intentions to view model, use [sendIntention] method
 *
 * There must be NOT BUSINESS LOGIC in here, so all shared logic can be tested in its respective part, just common UI stuff like:
 *
 * - inflating layout
 * - inject / release dependencies
 * - binding / unbinding to view model
 * - etc...
 */
abstract class MviActivity<Intention, State> :
    MviView<Intention, State>,
    AppCompatActivity() {

    private val intentionsSubject = PublishSubject.create<Intention>()

    private lateinit var interactor: MviInteractor<Intention, State>
    private lateinit var intentionsDisposable: Disposable

    private val statesCompositeDisposable = CompositeDisposable()

    // Expose the inflated view so you can use it for view binding
    @SuppressWarnings("WeakerAccess")
    protected lateinit var inflatedView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        declareScope(mviConfig)

        interactor = interactorFactory(mviConfig.scopeId)

        closeScopeProcess(interactor, mviConfig)

        super.onCreate(savedInstanceState)

        // View is ready to start sending intentions
        intentionsDisposable = interactor.subscribeToIntentions(intentions())

        // Inflate and build layout
        inflatedView = layoutInflater.inflate(mviConfig.layoutId, null)
        setContentView(inflatedView)

        afterComponentCreated(savedInstanceState)
    }

    /**
     * View is fully ready so subscribe to states
     */
    override fun onResume() {
        super.onResume()
        statesCompositeDisposable.add(interactor.states().subscribe(this::render))
    }

    /**
     * View is not ready anymore to render stuff
     */
    override fun onPause() {
        statesCompositeDisposable.clear()
        super.onPause()
    }

    /**
     * View is not ready anymore for anything
     */
    override fun onDestroy() {
        intentionsDisposable.dispose()
        super.onDestroy()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.fragments.lastOrNull()
        if (fragment != null && fragment is BackPressedObserver) {
            fragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun sendIntention(intention: Intention) {
        intentionsSubject.onNext(requireNotNull(intention))
    }

    override fun intentions(): Observable<Intention> {
        return intentionsSubject.hide()
    }
}
