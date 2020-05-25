package com.marcohc.terminator.core.mvi.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Completable
import io.reactivex.Single
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
    fun executeCompletable(function: (Fragment) -> Unit) = Completable.fromAction { execute(function::invoke) }

    /**
     * Executes the function and wraps it with Completable
     */
    fun executeCompletableWithActivity(function: (AppCompatActivity) -> Unit) = Completable.fromAction {
        execute { fragment ->
            fragment.activity?.run {
                function.invoke(this as AppCompatActivity)
            }
        }
    }

}

fun FragmentNavigationExecutor.getFragmentReady() = Single
    .create<Fragment> { emitter -> execute { fragment -> emitter.onSuccess(fragment) } }
    .observeOn(AndroidSchedulers.mainThread())

fun FragmentNavigationExecutor.getActivityReady() = Single
    .create<AppCompatActivity> { emitter ->
        execute { fragment ->
            fragment.activity?.run {
                emitter.onSuccess(this as AppCompatActivity)
            }
        }
    }
    .observeOn(AndroidSchedulers.mainThread())

class FragmentNavigationExecutorImpl : FragmentNavigationExecutor,
                                       LifecycleObserver {

    private var commandsList: MutableList<((Fragment) -> Unit)> = mutableListOf()
    private var isPaused: Boolean = true
    private var fragmentReference: WeakReference<Fragment?> = WeakReference(null)

    /**
     * Check if there was any navigation command to be executed when activity resumes
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        isPaused = false
        commandsList.forEach { command ->
            fragmentReference.get()?.let { fragment ->
                command(fragment)
            }
        }
        commandsList.clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        isPaused = true
    }

    /**
     * Remove itself from the lifecycle observers
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
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
