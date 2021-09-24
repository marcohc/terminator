package com.marcohc.terminator.core.analytics

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class FirebaseAnalyticsImpl(
    private val firebaseAnalytics: FirebaseAnalytics
) : Analytics {

    override fun trackAppOpened() {
        logEventInFirebase(FirebaseAnalytics.Event.APP_OPEN)
    }

    override fun trackClick(
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

    override fun trackEvent(
        eventId: String,
        bundle: Bundle
    ) {
        logEventInFirebase(eventId, bundle)
    }

    override fun trackCheckoutStart(
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

    override fun trackCheckoutEnd(
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

    override fun trackTutorialStarted() {
        logEventInFirebase(FirebaseAnalytics.Event.TUTORIAL_BEGIN)
    }

    override fun trackTutorialCompleted() {
        logEventInFirebase(FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
    }

    override fun trackScreen(screenId: String) {
        Timber.v("trackCurrentScreen: $screenId")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenId)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenId)
            }
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
