package com.marcohc.terminator.core.analytics

import android.app.Activity
import android.os.Bundle

interface Analytics {

    fun logAppOpened()

    fun logClick(
            screenId: String,
            itemId: String
    )

    fun logEvent(
            eventId: String,
            bundle: Bundle = Bundle()
    )

    fun logCheckoutStart(
            value: Double,
            currency: String
    )

    fun logCheckoutEnd(
            value: Double,
            currency: String
    )

    fun logTutorialStarted()

    fun logTutorialCompleted()

    fun logCurrentScreen(
            activity: Activity,
            screen: String
    )

}

