package com.marcohc.terminator.core.ads.native

import android.os.Bundle
import com.marcohc.terminator.core.ads.native.NativeEvent.*
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface NativeAnalytics {
    fun trackEvent(event: NativeEvent): Completable
}

internal class NativeAnalyticsImpl(
    private val analytics: Analytics,
    private val scopeId: String
) : NativeAnalytics {

    override fun trackEvent(event: NativeEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> trackEvents("Available")
            is FailedToLoad -> trackEvents("NotAvailable")
            is Opened -> trackEvents("Opened")
            is Impression -> trackEvents("Impression")
            is Click -> trackEvents("Click")
            is Closed -> trackEvents("Closed")
            else -> {
                // No-op
            }
        }
    }

    private fun trackEvents(event: String) {
        analytics.trackEvent(
            BASE_EVENT,
            Bundle().apply { putString("${BASE_EVENT}Action", "$scopeId$event") }
        )
        analytics.trackEvent("$scopeId$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Native"
    }
}
