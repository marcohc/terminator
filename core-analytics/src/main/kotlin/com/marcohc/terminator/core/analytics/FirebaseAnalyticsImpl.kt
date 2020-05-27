package com.marcohc.terminator.core.analytics

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class FirebaseAnalyticsImpl(
        private val firebaseAnalytics: FirebaseAnalytics
) : Analytics {

    override fun logAppOpened() {
        logEventInFirebase(FirebaseAnalytics.Event.APP_OPEN)
    }

    override fun logClick(
            screenId: String,
            itemId: String
    ) {
        logEventInFirebase(
            FirebaseAnalytics.Event.SELECT_CONTENT,
            createContentTypeBundle(
                screenId,
                itemId
            )
        )
    }

    override fun logEvent(
            eventId: String,
            bundle: Bundle
    ) {
        logEventInFirebase(eventId, bundle)
    }

    override fun logCheckoutStart(
            value: Double,
            currency: String
    ) {
        logEventInFirebase(
            FirebaseAnalytics.Event.BEGIN_CHECKOUT,
            Bundle().apply {
                putDouble(FirebaseAnalytics.Param.VALUE, value)
                putString(FirebaseAnalytics.Param.CURRENCY, currency)
            }
        )
    }

    override fun logCheckoutEnd(
            value: Double,
            currency: String
    ) {
        logEventInFirebase(
            FirebaseAnalytics.Event.PURCHASE,
            Bundle().apply {
                putDouble(FirebaseAnalytics.Param.VALUE, value)
                putString(FirebaseAnalytics.Param.CURRENCY, currency)
            }
        )
    }

    override fun logTutorialStarted() {
        logEventInFirebase(FirebaseAnalytics.Event.TUTORIAL_BEGIN)
    }

    override fun logTutorialCompleted() {
        logEventInFirebase(FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
    }

    override fun logCurrentScreen(activity: Activity, screen: String) {
        Timber.v("logCurrentScreen: $screen")
        firebaseAnalytics.setCurrentScreen(
            activity,
            screen,
            screen
        )
    }

    private fun createContentTypeBundle(
            screenId: String,
            itemId: String? = null
    ): Bundle {
        return Bundle()
            .apply {
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, screenId)
                if (itemId != null) putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            }
    }

    private fun logEventInFirebase(eventId: String, bundle: Bundle = Bundle()) {
        Timber.v("$eventId $bundle")
        firebaseAnalytics.logEvent(eventId, bundle)
    }

}
