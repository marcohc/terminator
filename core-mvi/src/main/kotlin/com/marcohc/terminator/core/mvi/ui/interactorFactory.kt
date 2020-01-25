package com.marcohc.terminator.core.mvi.ui

import android.app.Activity
import android.content.ComponentCallbacks
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
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
    when (mviConfig.mviConfigType) {
        MviConfigType.NO_SCOPE -> {
            // No-op
        }
        MviConfigType.SCOPE_ONLY, MviConfigType.SCOPE_AND_NAVIGATION -> {
            val scope = getKoin().getOrCreateScope(mviConfig.scopeId, named(mviConfig.scopeId))
            if (mviConfig.mviConfigType == MviConfigType.SCOPE_AND_NAVIGATION) {
                when (this) {
                    is AppCompatActivity -> {
                        try {
                            scope.get<ActivityNavigationExecutor>(named(mviConfig.scopeId))
                                .setActivity(this)
                        } catch (ignored: NoBeanDefFoundException) {
                            throw IllegalStateException("Ey developer, if you use navigation, add ActivityNavigationExecutor to your module")
                        }
                    }
                    is DialogFragment, is Fragment -> try {
                        scope.get<FragmentNavigationExecutor>(named(mviConfig.scopeId))
                            .setFragment(this as Fragment)
                    } catch (ignored: NoBeanDefFoundException) {
                        throw IllegalStateException("Ey developer, if you use navigation, add FragmentNavigationExecutor to your module")
                    } catch (ignored: ClassCastException) {
                        throw IllegalStateException("Ey developer, the activity of this fragment must be an AppCompatActivity")
                    }
                    else -> throw IllegalStateException("Ey developer, only AppCompatActivity, Fragment or DialogFragment is supported")
                }
            }
        }
    }
}

fun <Intention, State> ComponentCallbacks.interactorFactory(scopeId: String): MviInteractor<Intention, State> {
    return try {
        get(
            named(scopeId),
            parameters = { parametersOf(this) }
        )
    } catch (e: InstanceCreationException) {
        closeScope(scopeId)
        if (get(named(MVI_DEBUG))) {
            throw IllegalStateException("Ey developer, your Koin module is not properly setup", e)
        } else {
            Timber.w("Process killed: Create dummy interactor")
            createDummyInteractor()
        }
    }
}

fun <Intention, State> ComponentCallbacks.closeScopeProcess(interactor: MviInteractor<Intention, State>, mviConfig: MviConfig) {
    when (mviConfig.mviConfigType) {
        MviConfigType.NO_SCOPE -> {
            // No-op
        }
        MviConfigType.SCOPE_ONLY, MviConfigType.SCOPE_AND_NAVIGATION -> {
            // Only release the scope when the activity is really finishing not when OS kill it
            interactor.doOnDestroy {
                getActivity()?.run {
                    if (isFinishing) closeScope(mviConfig.scopeId)
                }
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

const val MVI_DEBUG = "MVI_DEBUG"

private fun ComponentCallbacks.closeScope(scopeId: String) {
    try {
        getKoin().getScope(scopeId).close()
    } catch (ignored: ScopeNotCreatedException) {
        // No-op
    }
}

private fun ComponentCallbacks.getActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is Fragment -> this.activity
        else -> null
    }
}
