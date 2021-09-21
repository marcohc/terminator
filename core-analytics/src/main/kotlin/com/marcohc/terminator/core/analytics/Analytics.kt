package com.marcohc.terminator.core.analytics

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment

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

    fun trackScreen(activity: Activity)

    fun trackScreen(fragment: Fragment)

}

