package com.marcohc.terminator.core.ads.native

import android.os.Bundle
import com.marcohc.terminator.core.ads.native.NativeEvent.Click
import com.marcohc.terminator.core.ads.native.NativeEvent.Closed
import com.marcohc.terminator.core.ads.native.NativeEvent.FailedToLoad
import com.marcohc.terminator.core.ads.native.NativeEvent.Impression
import com.marcohc.terminator.core.ads.native.NativeEvent.Loaded
import com.marcohc.terminator.core.ads.native.NativeEvent.Opened
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface NativeAnalytics {
    fun logEvent(event: NativeEvent): Completable
}

internal class NativeAnalyticsImpl(
        private val analytics: Analytics,
        private val scopeId: String
) : NativeAnalytics {

    override fun logEvent(event: NativeEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> logEvents("available")
            is FailedToLoad -> logEvents("not_available")
            is Opened -> logEvents("opened")
            is Impression -> logEvents("impression")
            is Click -> {
                analytics.trackClick(scopeId, "${BASE_EVENT}_click")
                logEvents("click")
            }
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
        const val BASE_EVENT = "native"
    }
}
