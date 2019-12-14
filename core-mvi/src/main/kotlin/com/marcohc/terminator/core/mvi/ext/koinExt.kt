package com.marcohc.terminator.core.mvi.ext

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.domain.ViewModelFactory
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutorImpl
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutorImpl
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

//region Activity Koin ext
inline fun <reified T> Module.declareScopedActivityRouter(scopeId: String, crossinline block: Scope.(ActivityNavigationExecutor) -> T) {
    declareScopedActivityNavigator(scopeId)
    scope(named(scopeId)) { scoped { block.invoke(this, getScope(scopeId).get(named(scopeId))) } }
}

inline fun <reified T> Module.declareFactoryActivityRouter(scopeId: String, crossinline block: Scope.(ActivityNavigationExecutor) -> T) {
    declareScopedActivityNavigator(scopeId)
    factory { block.invoke(this, getScope(scopeId).get(named(scopeId))) }
}

fun Module.declareScopedActivityNavigator(scopeId: String) = scope(named(scopeId)) {
    scoped<ActivityNavigationExecutor>(named(scopeId)) { ActivityNavigationExecutorImpl() }
}

inline fun <reified T : MviBaseInteractor<*, *, *>> Module.declareActivityInteractor(
        scopeId: String,
        attachToLifecycleModule: Module? = null,
        crossinline interactorFactoryFunction: Scope.() -> T
) {
    factory(named(scopeId)) { (appCompatActivity: AppCompatActivity) ->
        ViewModelProviders
            .of(appCompatActivity, ViewModelFactory {
                createInteractorInstance(
                    appCompatActivity,
                    attachToLifecycleModule,
                    interactorFactoryFunction
                )
            })
            .get(T::class.java)
    }
}
//endregion

//region Fragment Koin ext
inline fun <reified T> Module.declareFactoryFragmentRouter(scopeId: String, crossinline block: Scope.(FragmentNavigationExecutor) -> T) {
    declareScopedFragmentNavigator(scopeId)
    factory { block.invoke(this, getScope(scopeId).get(named(scopeId))) }
}

fun Module.declareScopedFragmentNavigator(scopeId: String) = scope(named(scopeId)) {
    scoped<FragmentNavigationExecutor>(named(scopeId)) { FragmentNavigationExecutorImpl() }
}

inline fun <reified T : MviBaseInteractor<*, *, *>> Module.declareFragmentInteractor(
        scopeId: String,
        attachToLifecycleModule: Module? = null,
        crossinline interactorFactoryFunction: Scope.() -> T
) {
    factory(named(scopeId)) { (fragment: Fragment) ->
        ViewModelProviders
            .of(fragment, ViewModelFactory {
                createInteractorInstance(
                    fragment.activity,
                    attachToLifecycleModule,
                    interactorFactoryFunction
                )
            })
            .get(T::class.java)
    }
}
//endregion

inline fun <reified T : MviBaseInteractor<*, *, *>> Scope.createInteractorInstance(
        activity: Activity?,
        moduleToAttachToLifecycle: Module?,
        interactorFactoryFunction: Scope.() -> T
): T {
    moduleToAttachToLifecycle?.let { loadKoinModules(it) }
    return interactorFactoryFunction.invoke(this)
        .apply {
            moduleToAttachToLifecycle?.let {
                doOnDestroy {
                    // Only release attached module when the activity is really finishing not when OS kill it
                    if (activity == null || activity.isFinishing) {
                        unloadKoinModules(moduleToAttachToLifecycle)
                    }
                }
            }
        }
}
