package com.marcohc.terminator.core.analytics

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class FirebaseAnalyticsImpl(
        private val firebaseAnalytics: FirebaseAnalytics
) : Analytics {

    override fun logAppOpened() {
        logEvent(FirebaseAnalytics.Event.APP_OPEN)
    }

    override fun logClick(
            screenId: String,
            itemId: String,
            itemName: String?
    ) {
        logEvent(
            FirebaseAnalytics.Event.SELECT_CONTENT,
            createContentTypeBundle(
                screenId,
                itemId,
                itemName
            )
        )
    }

    override fun logView(
            itemId: String,
            itemName: String,
            itemCategory: String
    ) {
        logEvent(
            FirebaseAnalytics.Event.VIEW_ITEM,
            createViewBundle(
                itemId = itemId,
                itemName = itemName,
                itemCategory = itemCategory
            )
        )
    }

    override fun logCustomEvent(
            eventId: String
    ) {
        logEvent(eventId)
    }

    override fun logCheckoutStart(
            option: String,
            value: Double,
            currency: String
    ) {
        val bundle = Bundle()
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, value)
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency)
        logEvent(
            FirebaseAnalytics.Event.BEGIN_CHECKOUT,
            bundle
        )
    }

    override fun logCheckoutProgress(
            option: String,
            step: Long
    ) {
        val bundle = Bundle()
        bundle.putLong(FirebaseAnalytics.Param.CHECKOUT_STEP, step)
        bundle.putString(FirebaseAnalytics.Param.CHECKOUT_OPTION, option)
        logEvent(FirebaseAnalytics.Event.CHECKOUT_PROGRESS, bundle)
    }

    override fun logCheckoutEnd(
            screen: String,
            option: String,
            value: Double,
            currency: String
    ) {
        val bundle = Bundle()
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, value)
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency)
        logEvent(
            FirebaseAnalytics.Event.ECOMMERCE_PURCHASE,
            bundle
        )
    }

    override fun logTutorialStarted() {
        logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN)
    }

    override fun logTutorialCompleted() {
        logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
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
            itemId: String? = null,
            itemName: String? = null
    ): Bundle {
        return Bundle()
            .apply {
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, screenId)
                if (itemId != null) putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
                if (itemName != null) putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            }
    }

    private fun createViewBundle(
            itemId: String,
            itemName: String,
            itemCategory: String
    ): Bundle {
        return Bundle()
            .apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
                putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
                putString(FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory)
            }
    }

    private fun logEvent(eventId: String, bundle: Bundle = Bundle()) {
        Timber.v("logEvent: $eventId $bundle")
        firebaseAnalytics.logEvent(eventId, bundle)
    }

}
