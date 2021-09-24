package com.marcohc.terminator.core.analytics

import android.os.Bundle

interface Analytics {

    fun trackAppOpened()

    @Deprecated("Use trackEvent instead")
    fun trackClick(
        screenId: String,
        itemId: String
    )

    fun trackEvent(
        eventId: String,
        bundle: Bundle = Bundle()
    )

    fun trackCheckoutStart(
        value: Double,
        currency: String
    )

    fun trackCheckoutEnd(
        value: Double,
        currency: String
    )

    fun trackTutorialStarted()

    fun trackTutorialCompleted()

    fun trackScreen(screenId: String)

}

