@file:Suppress("unused")

package com.marcohc.terminator.core.mvi.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import java.lang.ref.WeakReference

/**
 * Class to delegate Android navigation and make it unit testable.
 *
 * It checks weather the activity is ready or not to execute UI navigation commands.
 *
 * You must tight the scope of the FragmentNavigationExecutor to the activity and call setActivity in onCreate method.
 */
interface FragmentNavigationExecutor {

    /**
     * Sets the activity you want to use for navigation
     */
    fun setFragment(fragment: Fragment)

    /**
     * Executes a block of navigation code if the activity is ready, otherwise will store the command and will be executed onResume
     */
    fun execute(command: (Fragment) -> Unit)

    /**
     * Executes a block of navigation code if the activity is not null, otherwise it won't be executed
     */
    fun executeWithActivity(command: (AppCompatActivity) -> Unit) = execute { fragment ->
        fragment.activity?.run {
            command.invoke(this as AppCompatActivity)
        }
    }

    /**
     * Executes the function and wraps it with Completable
     */
    fun executeCompletable(function: (Fragment) -> Unit) =
        Completable.fromAction { execute(function::invoke) }

    /**
     * Executes the function and wraps it with Completable
     */
    fun executeCompletableWithActivity(function: (AppCompatActivity) -> Unit) = getActivityReady()
        .flatMapCompletable { activity -> Completable.fromAction { function.invoke(activity) } }

    fun <T> executeSingle(function: (Pair<Fragment, SingleEmitter<T>>) -> Unit): Single<T> {
        return getFragmentReady()
            .flatMap { fragment ->
                // This cast must be here
                Single.create<T> { emitter -> function.invoke(fragment to emitter) }
            }
    }

    fun <T> executeSingleWithActivity(function: (Pair<AppCompatActivity, SingleEmitter<T>>) -> Unit): Single<T> {
        return getActivityReady()
            .flatMap { activity: AppCompatActivity ->
                // This cast must be here
                Single.create<T> { emitter -> function.invoke(activity to emitter) }
            }
    }

    fun <T> executeObservableWithActivity(function: (Pair<AppCompatActivity, ObservableEmitter<T>>) -> Unit): Observable<T> =
        getActivityReady()
            .toObservable()
            .flatMap { activity: AppCompatActivity ->
                // This cast must be here
                Observable.create<T> { emitter -> function.invoke(activity to emitter) }
            }

    fun <T> executeObservable(function: (Pair<Fragment, ObservableEmitter<T>>) -> Unit): Observable<T> =
        getFragmentReady()
            .toObservable()
            .flatMap { fragment: Fragment ->
                // This cast must be here
                Observable.create<T> { emitter -> function.invoke(fragment to emitter) }
            }

    fun getFragmentReady() = Single
        .create<Fragment> { emitter -> execute { fragment -> emitter.onSuccess(fragment) } }
        .observeOn(AndroidSchedulers.mainThread())

    fun getActivityReady() = Single
        .create<AppCompatActivity> { emitter ->
            execute { fragment ->
                fragment.activity?.run {
                    emitter.onSuccess(this as AppCompatActivity)
                }
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
}

class FragmentNavigationExecutorImpl :
    FragmentNavigationExecutor,
    DefaultLifecycleObserver {

    private var commandsList: MutableList<((Fragment) -> Unit)> = mutableListOf()
    private var isPaused: Boolean = true
    private var fragmentReference: WeakReference<Fragment?> = WeakReference(null)

    /**
     * Check if there was any navigation command to be executed when activity resumes
     */
    override fun onResume(owner: LifecycleOwner) {
        isPaused = false
        commandsList.forEach { command ->
            fragmentReference.get()?.let { fragment ->
                command(fragment)
            }
        }
        commandsList.clear()
    }

    override fun onPause(owner: LifecycleOwner) {
        isPaused = true
    }

    /**
     * Remove itself from the lifecycle observers
     */
    override fun onDestroy(owner: LifecycleOwner) {
        fragmentReference.get()?.lifecycle?.removeObserver(this)
    }

    override fun setFragment(fragment: Fragment) {
        fragmentReference = WeakReference(fragment)
        fragment.lifecycle.removeObserver(this)
        fragment.lifecycle.addObserver(this)
    }

    @Suppress("ComplexCondition")
    override fun execute(command: (Fragment) -> Unit) {
        fragmentReference.get()?.let { fragment ->
            if (isPaused || fragment.activity == null || fragment.isDetached || !fragment.isAdded || fragment.view == null) {
                commandsList.add(command)
            } else {
                command(fragment)
            }
        }
    }
}
