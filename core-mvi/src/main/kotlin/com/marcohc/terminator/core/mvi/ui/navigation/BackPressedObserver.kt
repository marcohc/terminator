package com.marcohc.terminator.core.mvi.ui.navigation

/**
 * This interface is meant to be used by a Fragment which one to be notified by its [MviActivity] that a back pressed event has occured.
 */
interface BackPressedObserver {

    fun onBackPressed()

}
