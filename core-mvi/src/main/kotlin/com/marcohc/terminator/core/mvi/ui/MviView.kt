package com.marcohc.terminator.core.mvi.ui

import android.os.Bundle
import android.view.View
import io.reactivex.Observable

/**
 * Interface for a view to work with [MviInteractor] following MVI
 */
interface MviView<Intention, State> {

    /**
     * Configuration for the view
     */
    val mviConfig: MviConfig

    /**
     * This method will be called after onCreate of the activity.
     *
     * Use it for one time actions and layout building.
     */
    fun afterComponentCreated(savedInstanceState: Bundle?)

    /**
     * Input pipeline for intentions.
     *
     * Way to comunnicate to whoever wants to listen to this events.
     */
    fun intentions(): Observable<Intention>

    /**
     * Output pipeline for states.
     *
     * Endless fresh states will arrive throught this method
     */
    fun render(state: State)

    /**
     * You as a developer will use this method to send intentions throught the pipeline.
     */
    fun sendIntention(intention: Intention)

    /**
     * View extension methods
     */

    fun View.sendIntentionOnClick(function: () -> Intention) {
        setOnClickListener { sendIntention(function.invoke()) }
    }

    fun View.sendIntentionOnLongClick(function: () -> Intention) {
        setOnLongClickListener {
            sendIntention(function.invoke())
            true
        }
    }

}
