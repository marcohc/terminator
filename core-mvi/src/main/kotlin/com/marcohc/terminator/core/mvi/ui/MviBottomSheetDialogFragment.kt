package com.marcohc.terminator.core.mvi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.marcohc.terminator.core.mvi.domain.MviInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class is the base class for a fragment which wants to implement MVI architecture.
 *
 * Just extend your Fragment and fill out the specific variables for it.
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
@Suppress("TooManyFunctions")
abstract class MviBottomSheetDialogFragment<Intention, State>
    : MviView<Intention, State>,
    BottomSheetDialogFragment() {

    private val intentionsSubject: PublishSubject<Intention> = PublishSubject.create()

    private lateinit var interactor: MviInteractor<Intention, State>
    private lateinit var intentionsDisposable: Disposable

    private var statesCompositeDisposable = CompositeDisposable()
    private val isFirstTime = AtomicBoolean(true)

    // Expose the inflated view so you can it for view binding
    @SuppressWarnings("WeakerAccess")
    protected lateinit var inflatedView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpInteractor()

        super.onCreate(savedInstanceState)

        // View is ready to start sending intentions
        intentionsDisposable = interactor.subscribeToIntentions(intentions())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment or reuse the existing one
        if (!::inflatedView.isInitialized) {
            inflatedView = inflater.inflate(mviConfig.layoutId, container, false)
        }
        return inflatedView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isFirstTime.getAndSet(false)) {
            afterComponentCreated(savedInstanceState)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        // We must remove the view from the parent when it's a nested fragment
        view?.let {
            (it.parent as? ViewGroup)?.removeView(inflatedView)
        }
    }

    override fun sendIntention(intention: Intention) {
        intentionsSubject.onNext(requireNotNull(intention))
    }

    override fun intentions(): Observable<Intention> {
        return intentionsSubject.hide()
    }

    private fun setUpInteractor() {
        declareScope(mviConfig)

        interactor = interactorFactory(mviConfig.scopeId)

        closeScopeProcess(interactor, mviConfig)
    }
}
