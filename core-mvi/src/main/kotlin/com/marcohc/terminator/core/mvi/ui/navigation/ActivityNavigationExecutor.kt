@file:Suppress("unused")

package com.marcohc.terminator.core.mvi.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import java.lang.ref.WeakReference

/**
 * Class to delegate Android navigation and make it unit testable.
 *
 * It checks weather the activity is ready or not to execute UI navigation commands.
 *
 * You must tight the scope of the ActivityNavigationExecutor to the activity and call setActivity in onCreate method.
 */
interface ActivityNavigationExecutor {

    /**
     * Sets the activity you want to use for navigation
     */
    fun setActivity(activity: AppCompatActivity)

    /**
     * Executes a block of navigation code if the activity is ready, otherwise will store the command and will be executed onResume
     */
    fun execute(command: (activity: AppCompatActivity) -> Unit)

    /**
     * Executes the block and wraps it with Completable
     */
    fun executeCompletable(function: (AppCompatActivity) -> Unit) = getActivityReady()
        .flatMapCompletable { activity -> Completable.fromAction { function.invoke(activity) } }

    fun <T> executeSingle(function: (Pair<AppCompatActivity, SingleEmitter<T>>) -> Unit) =
        getActivityReady()
            .flatMap { activity: AppCompatActivity ->
                // This cast must be here
                Single.create<T> { emitter -> function.invoke(activity to emitter) }
            }

    fun <T> executeObservable(function: (Pair<AppCompatActivity, ObservableEmitter<T>>) -> Unit): Observable<T> =
        getActivityReady()
            .toObservable()
            .flatMap { activity: AppCompatActivity ->
                // This cast must be here
                Observable.create<T> { emitter -> function.invoke(activity to emitter) }
            }

    fun getActivityReady() = Single
        .create<AppCompatActivity> { emitter -> execute { activity -> emitter.onSuccess(activity) } }
        .observeOn(AndroidSchedulers.mainThread())
}

class ActivityNavigationExecutorImpl : ActivityNavigationExecutor,
    DefaultLifecycleObserver {

    private var commandsList: MutableList<((AppCompatActivity) -> Unit)> = mutableListOf()
    private var isPaused: Boolean = true
    private var activityWeakReference: WeakReference<AppCompatActivity?> = WeakReference(null)

    /**
     * Check if there was any navigation command to be executed when activity resumes
     */
    override fun onResume(owner: LifecycleOwner) {
        isPaused = false
        commandsList.forEach { command ->
            activityWeakReference.get()?.let { activity ->
                command(activity)
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
        activityWeakReference.get()?.lifecycle?.removeObserver(this)
    }

    override fun setActivity(activity: AppCompatActivity) {
        activityWeakReference = WeakReference(activity)
        activity.lifecycle.removeObserver(this)
        activity.lifecycle.addObserver(this)
    }

    override fun execute(command: (activity: AppCompatActivity) -> Unit) {
        activityWeakReference.get()?.let { activity ->
            if (isPaused || activity.isFinishing || activity.isDestroyed) {
                commandsList.add(command)
            } else {
                command(activity)
            }
        }
    }
}
