package com.marcohc.terminator.core.ads.interstitial

import android.os.Bundle
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.Closed
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.FailedToLoad
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.Loaded
import com.marcohc.terminator.core.ads.interstitial.InterstitialEvent.Opened
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface InterstitialAnalytics {
    fun logEvent(event: InterstitialEvent): Completable
}

internal class InterstitialAnalyticsImpl(
    private val analytics: Analytics,
    private val scopeId: String
) : InterstitialAnalytics {

    override fun logEvent(event: InterstitialEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> logEvents("available")
            is FailedToLoad -> logEvents("not_available")
            is Opened -> logEvents("opened")
            is Closed -> logEvents("closed")
            else -> {
                // No-op
            }
        }
    }

    private fun logEvents(parameter: String) {
        analytics.trackEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}_action", "${scopeId}_${parameter}") })
        analytics.trackEvent("${scopeId}_${BASE_EVENT}_${parameter}")
    }

    private companion object {
        const val BASE_EVENT = "interstitial"
    }
}
