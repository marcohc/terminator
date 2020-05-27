package com.marcohc.terminator.core.mvi.ui

import android.content.ComponentCallbacks
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.marcohc.terminator.core.mvi.BuildConfig
import com.marcohc.terminator.core.mvi.MviConstants.SCOPE_LOG_TAG
import com.marcohc.terminator.core.mvi.domain.MviInteractor
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutor
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.core.error.InstanceCreationException
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.error.ScopeNotCreatedException
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import timber.log.Timber

fun ComponentCallbacks.declareScope(mviConfig: MviConfig) {
    with(mviConfig) {
        when (mviConfigType) {
            MviConfigType.NO_SCOPE -> {
                // No-op
            }
            MviConfigType.SCOPE_ONLY, MviConfigType.SCOPE_AND_NAVIGATION -> {
                Timber.v(SCOPE_LOG_TAG, "Opening scope $scopeId")
                val scope = getKoin().getOrCreateScope(scopeId, named(scopeId))
                if (mviConfigType == MviConfigType.SCOPE_AND_NAVIGATION) {
                    when (this@declareScope) {
                        is AppCompatActivity -> {
                            Timber.v(SCOPE_LOG_TAG, "Biding Activity with Executor in $scopeId")
                            try {
                                scope.get<ActivityNavigationExecutor>(named(scopeId))
                                    .setActivity(this@declareScope)
                            } catch (ignored: NoBeanDefFoundException) {
                                throw IllegalStateException("Ey developer, if you use navigation, use declare[Factory / Scoped]ActivityRouter in your Module")
                            }
                        }
                        is DialogFragment, is Fragment -> try {
                            Timber.v(SCOPE_LOG_TAG, "Biding Fragment with Executor in $scopeId")
                            scope.get<FragmentNavigationExecutor>(named(scopeId))
                                .setFragment(this@declareScope as Fragment)
                        } catch (ignored: NoBeanDefFoundException) {
                            throw IllegalStateException("Ey developer, if you use navigation, use declare[Factory / Scoped]FragmentRouter in your Module")
                        } catch (ignored: ClassCastException) {
                            throw IllegalStateException("Ey developer, the activity of this fragment must be an AppCompatActivity")
                        }
                        else -> throw IllegalStateException("Ey developer, only AppCompatActivity, Fragment or DialogFragment is supported")
                    }
                }
            }
        }
    }
}

fun <Intention, State> ComponentCallbacks.interactorFactory(scopeId: String): MviInteractor<Intention, State> {
    return try {
        get(
            qualifier = named(scopeId),
            parameters = { parametersOf(this) }
        )
    } catch (throwable: Throwable) {
        when (throwable) {
            is InstanceCreationException, is ScopeNotCreatedException -> {
                closeScope(scopeId)
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("Ey developer, your Koin module is not properly setup", throwable)
                } else {
                    Timber.w(SCOPE_LOG_TAG, "Couldn't create Interactor, creating a dummy one")
                    createDummyInteractor()
                }
            }
            else -> {
                Timber.w(SCOPE_LOG_TAG, "Non caught exception")
                throw throwable
            }
        }
    }
}

fun <Intention, State> ComponentCallbacks.closeScopeProcess(interactor: MviInteractor<Intention, State>, mviConfig: MviConfig) {
    when (mviConfig.mviConfigType) {
        MviConfigType.NO_SCOPE -> {
            // No-op
        }
        MviConfigType.SCOPE_ONLY, MviConfigType.SCOPE_AND_NAVIGATION -> {
            // Only release the scope when the activity/fragment is really finishing not when OS kill it
            interactor.doOnDestroy {
                if (isFinishing()) closeScope(mviConfig.scopeId)
            }
        }
    }
}

private fun <Intention, State> createDummyInteractor(): MviInteractor<Intention, State> {
    return object : MviInteractor<Intention, State> {

        override fun subscribeToIntentions(intentions: Observable<Intention>): Disposable {
            return Observable.never<Intention>().subscribe()
        }

        override fun states(): Observable<State> {
            return Observable.never()
        }

        override fun doOnDestroy(onDestroyFunction: () -> Unit) {
            // No-op
        }
    }
}

private fun ComponentCallbacks.closeScope(scopeId: String) {
    try {
        Timber.v(SCOPE_LOG_TAG, "Closing scope $scopeId")
        getKoin().getScope(scopeId).close()
    } catch (e: ScopeNotCreatedException) {
        Timber.w(SCOPE_LOG_TAG, "Closing scope $scopeId failed: ${e.message}")
    }
}
