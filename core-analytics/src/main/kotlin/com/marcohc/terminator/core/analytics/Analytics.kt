package com.marcohc.terminator.core.analytics

import android.app.Activity

interface Analytics {

    fun logAppOpened()

    fun logClick(
            screenId: String,
            itemId: String,
            itemName: String? = null
    )

    fun logView(
            itemId: String,
            itemName: String,
            itemCategory: String
    )

    fun logCustomEvent(
            eventId: String
    )

    fun logCheckoutStart(
            option: String,
            value: Double,
            currency: String
    )

    fun logCheckoutProgress(
            option: String,
            step: Long
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

