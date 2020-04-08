package com.marcohc.terminator.core.analytics

import android.app.Activity
import android.os.Bundle

interface Analytics {

    fun logAppOpened()

    fun logClick(
            screenId: String,
            itemId: String
    )

    fun logCustomEvent(
            eventId: String,
            bundle: Bundle
    )

    fun logCheckoutStart(
            option: String,
            value: Double,
            currency: String
    )

    fun logCheckoutEnd(
            screen: String,
            option: String,
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

