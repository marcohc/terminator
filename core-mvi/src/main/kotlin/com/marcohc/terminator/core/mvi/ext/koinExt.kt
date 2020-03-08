@file:Suppress("unused")

package com.marcohc.terminator.core.mvi.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.domain.MviInteractor
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
        moduleToAttachToLifecycle: Module? = null,
        crossinline interactorFactoryFunction: Scope.(AppCompatActivity) -> T
) {
    factory(named(scopeId)) { (appCompatActivity: AppCompatActivity) ->
        return@factory ViewModelProvider(appCompatActivity, ViewModelFactory {
            moduleToAttachToLifecycle?.let { loadKoinModules(it) }
            interactorFactoryFunction.invoke(this, appCompatActivity)
                .apply {
                    moduleToAttachToLifecycle?.let {
                        doOnDestroy {
                            // Only release attached module when the activity is really finishing not when OS kill it
                            if (appCompatActivity.isFinishing) {
                                unloadKoinModules(moduleToAttachToLifecycle)
                            }
                        }
                    }
                }
        }).get(T::class.java) as MviInteractor<*, *> // This "useless" cast is necessary for Koin
    }
}

inline fun <reified T> Module.declareFactoryFragmentRouter(scopeId: String, crossinline block: Scope.(FragmentNavigationExecutor) -> T) {
    declareScopedFragmentNavigator(scopeId)
    factory { block.invoke(this, getScope(scopeId).get(named(scopeId))) }
}

fun Module.declareScopedFragmentNavigator(scopeId: String) = scope(named(scopeId)) {
    scoped<FragmentNavigationExecutor>(named(scopeId)) { FragmentNavigationExecutorImpl() }
}

inline fun <reified T : MviBaseInteractor<*, *, *>> Module.declareFragmentInteractor(
        scopeId: String,
        moduleToAttachToLifecycle: Module? = null,
        crossinline interactorFactoryFunction: Scope.(Fragment) -> T
) {
    factory(named(scopeId)) { (fragment: Fragment) ->
        ViewModelProvider(fragment, ViewModelFactory {
            moduleToAttachToLifecycle?.let { loadKoinModules(it) }
            interactorFactoryFunction.invoke(this, fragment)
                .apply {
                    moduleToAttachToLifecycle?.let {
                        doOnDestroy {
                            // Only release attached module when the activity is really finishing not when OS kill it
                            if (fragment.activity == null || requireNotNull(fragment.activity).isFinishing) {
                                unloadKoinModules(moduleToAttachToLifecycle)
                            }
                        }
                    }
                }
        }).get(T::class.java) as MviInteractor<*, *> // This "useless" cast is necessary for Koin
    }
}
