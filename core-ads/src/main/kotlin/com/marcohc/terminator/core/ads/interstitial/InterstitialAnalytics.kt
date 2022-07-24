package com.marcohc.terminator.core.ads.interstitial

import android.os.Bundle
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.*
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

internal class InterstitialAnalytics(
    private val analytics: Analytics
) {

    fun trackEvent(event: InterstitialEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> trackEvents("Available")
            is FailedToLoad -> trackEvents("NotAvailable")
            is Opened -> trackEvents("Opened")
            is Impression -> trackEvents("Impression")
            is Clicked -> trackEvents("Clicked")
            is Closed -> trackEvents("Closed")
            is Paid -> trackEvents("Paid")
            Loading -> {
                // No-op
            }
        }
    }

    private fun trackEvents(event: String) {
        analytics.trackEvent(
            BASE_EVENT,
            Bundle().apply { putString("${BASE_EVENT}Action", event) })
        analytics.trackEvent("$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Interstitial"
    }
}
