package com.marcohc.terminator.core.mvi.ui

/**
 * Configures the behavior regarding scope and navigation
 */
enum class MviConfigType {
    /**
     * No scope is created
     */
    NO_SCOPE,

    /**
     * Creates an scope which will live withing the ViewModel lifecycle
     */
    SCOPE_ONLY,

    /**
     * Creates an scope which will live withing the ViewModel lifecycle
     * and attaches the view for the navigation.
     *
     * See [com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutor]
     */
    SCOPE_AND_NAVIGATION
}
