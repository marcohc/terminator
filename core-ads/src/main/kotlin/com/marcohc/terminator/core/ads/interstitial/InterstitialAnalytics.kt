package com.marcohc.terminator.core.ads.interstitial

import android.os.Bundle
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.Closed
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.FailedToLoad
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.Loaded
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.Opened
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface InterstitialAnalytics {
    fun trackEvent(event: InterstitialEvent): Completable
}

internal class InterstitialAnalyticsImpl(
    private val analytics: Analytics,
    private val scopeId: String
) : InterstitialAnalytics {

    override fun trackEvent(event: InterstitialEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> trackEvents("Available")
            is FailedToLoad -> trackEvents("NotAvailable")
            is Opened -> trackEvents("Opened")
            is Closed -> trackEvents("Closed")
            else -> {
                // No-op
            }
        }
    }

    private fun trackEvents(event: String) {
        analytics.trackEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}Action", "$scopeId$event") })
        analytics.trackEvent("$scopeId$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Interstitial"
    }
}
