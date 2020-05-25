@file:Suppress("unused")

package com.marcohc.terminator.core.mvi.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.marcohc.terminator.core.koin.FeatureModule
import com.marcohc.terminator.core.mvi.MviConstants.SCOPE_LOG_TAG
import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.domain.MviInteractor
import com.marcohc.terminator.core.mvi.domain.ViewModelFactory
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutorImpl
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutor
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutorImpl
import org.koin.core.context.loadKoinModules
import org.koin.core.error.NoScopeDefFoundException
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber

inline fun <reified T> Module.declareScopedActivityRouter(scopeId: String, crossinline block: Scope.(ActivityNavigationExecutor) -> T) {
    scope(named(scopeId)) {
        // Declare navigator
        scoped<ActivityNavigationExecutor>(named(scopeId)) { ActivityNavigationExecutorImpl() }
        // Declare router passing the executor
        scoped { block.invoke(this, getScope(scopeId).get(named(scopeId))) }
    }
}

inline fun <reified T> Module.declareFactoryActivityRouter(scopeId: String, crossinline block: Scope.(ActivityNavigationExecutor) -> T) {
    scope(named(scopeId)) {
        // Declare navigator
        scoped<ActivityNavigationExecutor>(named(scopeId)) { ActivityNavigationExecutorImpl() }
    }
    factory {
        // Declare router passing the executor
        block.invoke(this, getScope(scopeId).get(named(scopeId)))
    }
}

inline fun <reified T> Module.declareFactoryFragmentRouter(scopeId: String, crossinline block: Scope.(FragmentNavigationExecutor) -> T) {
    scope(named(scopeId)) {
        // Declare navigator
        scoped<FragmentNavigationExecutor>(named(scopeId)) { FragmentNavigationExecutorImpl() }
    }
    factory {
        // Declare router passing the executor
        block.invoke(this, getScope(scopeId).get(named(scopeId)))
    }
}

inline fun <reified T> Scope.defineInScope(scopeId: String, crossinline function: () -> T) {
    Timber.v(SCOPE_LOG_TAG, "Define in scope: scopeId: $scopeId / value: ${T::class.java.simpleName}")
    loadKoinModules(
        module {
            scope(named(scopeId)) {
                scoped { function.invoke() }
            }
        }
    )
}

inline fun <reified T : MviBaseInteractor<*, *, *>> Module.declareActivityInteractor(
        scopeId: String,
        associateScopeIdsWith: List<String> = emptyList(),
        crossinline interactorFactoryFunction: Scope.(AppCompatActivity) -> T
) {
    factory(named(scopeId)) { (appCompatActivity: AppCompatActivity) ->
        return@factory ViewModelProvider(appCompatActivity, ViewModelFactory {
            linkChildScopes(scopeId, associateScopeIdsWith)
            interactorFactoryFunction.invoke(this, appCompatActivity)
                .apply {
                    unlinkChildScopes(scopeId, associateScopeIdsWith) {
                        appCompatActivity.isFinishing
                    }
                }
        }).get(T::class.java) as MviInteractor<*, *> // This "useless" cast is necessary for Koin
    }
}

inline fun <reified T : MviBaseInteractor<*, *, *>> Module.declareFragmentInteractor(
        scopeId: String,
        associateScopeIdsWith: List<String> = emptyList(),
        crossinline interactorFactoryFunction: Scope.(Fragment) -> T
) {
    factory(named(scopeId)) { (fragment: Fragment) ->
        ViewModelProvider(fragment, ViewModelFactory {
            linkChildScopes(scopeId, associateScopeIdsWith)
            interactorFactoryFunction.invoke(this, fragment)
                .apply {
                    unlinkChildScopes(scopeId, associateScopeIdsWith) {
                        fragment.activity == null || requireNotNull(fragment.activity).isFinishing
                    }
                }
        }).get(T::class.java) as MviInteractor<*, *> // This "useless" cast is necessary for Koin
    }
}

/**
 * Link this scope with others to declare their dependencies within this scope.
 *
 * This is needed when using a module which is used multiple times, ie:
 *
 * Module A -> Module (child) X
 * Module B -> Module (child) X
 *
 * Each of them want to have X dependency within their own scope
 *
 */
fun Scope.linkChildScopes(scopeId: String, scopesIdsToAssociateWith: List<String>) {
    scopesIdsToAssociateWith.forEach { scopeIdToAssociate ->

        val childScope = getOrCreateScope(scopeIdToAssociate)
        var scopeCounter = childScope.getOrNull<ScopeCounter>()
        if (scopeCounter == null) {
            Timber.v(SCOPE_LOG_TAG, "Associate child ids with: $scopeId / childScope: $childScope / empty scopeCounter")
            scopeCounter = ScopeCounter().apply { add(scopeId) }
            defineInScope(scopeIdToAssociate) { scopeCounter }
        } else {
            Timber.v(SCOPE_LOG_TAG, "Associate child ids with: $scopeId / childScope: $childScope / scopeCounter: $scopeCounter")
            scopeCounter.add(scopeId)
        }
    }
}

/**
 *
 * When this scope is destroyed, then release itself from the internal [ScopeCounter] and if it's the last one, close it
 *
 */
inline fun <reified T : MviBaseInteractor<*, *, *>> T.unlinkChildScopes(
        scopeId: String,
        associateScopeIdsWith: List<String>,
        crossinline isFinishingFunction: () -> Boolean
) {
    if (associateScopeIdsWith.isNotEmpty()) {
        doOnDestroy {
            // Only release attached module when the activity is really finishing not when OS kill it
            if (isFinishingFunction.invoke()) {
                associateScopeIdsWith.forEach { scopeIdToAssociate ->
                    val childScope = getKoin().getOrCreateScope(scopeIdToAssociate, named(scopeIdToAssociate))
                    val scopeCounter = childScope.get<ScopeCounter>().apply { remove(scopeId) }
                    Timber.v(SCOPE_LOG_TAG, "Remove scopeId: $scopeId / childScope: $childScope / scopeCounter: $scopeCounter")
                    if (scopeCounter.isEmpty()) {
                        Timber.v(SCOPE_LOG_TAG, "Closing childScope: $childScope")
                        childScope.close()
                    }
                }
            }
        }
    }
}

/**
 * Using the counter associated with this [libraryScopeId], fetches or creates the value [T] from the parent scope
 * on top of the counter
 */
inline fun <reified T> Scope.fetchOrCreateFromParentScope(libraryScopeId: String, function: () -> T): T {

    val libraryScope = getScope(libraryScopeId)
    val scopeCounter = libraryScope.get<ScopeCounter>()
    val parentScopeId = scopeCounter.getOnTop()
    var value = getScope(parentScopeId).getOrNull<T>()

    return if (value == null) {
        value = function.invoke()
        Timber.v(SCOPE_LOG_TAG, "Fetch and create from: libraryScopeId: $libraryScopeId / parentScopeId: $parentScopeId / new value: $value")
        defineInScope(parentScopeId) { value }
        value
    } else {
        Timber.v(SCOPE_LOG_TAG, "Fetch from: libraryScopeId: $libraryScopeId / parentScopeId: $parentScopeId / existing value: $value")
        value
    }
}

/**
 * Use it from within the child module to fetch its parent scope
 */
fun FeatureModule.getParentScope(): Scope {
    with(getKoin()) {
        return getScope(getScope(scopeId).get<ScopeCounter>().getOnTop())
    }
}

private fun Scope.getOrCreateScope(scopeIdToAssociate: String): Scope {
    return try {
        getKoin().getOrCreateScope(scopeIdToAssociate, named(scopeIdToAssociate))
    } catch (e: NoScopeDefFoundException) {
        Timber.w(SCOPE_LOG_TAG, "Scope $scopeIdToAssociate was not defined, defining it...")
        loadKoinModules(module { scope(named(scopeIdToAssociate)) {} })
        getOrCreateScope(scopeIdToAssociate)
    }
}
