package com.marcohc.terminator.core.ads.banner

import android.os.Bundle
import com.marcohc.terminator.core.ads.banner.BannerEvent.*
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

internal class BannerAnalytics(
    private val analytics: Analytics
) {

    fun trackEvent(event: BannerEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> trackEvents("Available")
            is FailedToLoad -> trackEvents("NotAvailable")
            is Opened -> trackEvents("Opened")
            is Impression -> trackEvents("Impression")
            is Click -> trackEvents("Click")
            is Closed -> trackEvents("Closed")
        }
    }

    private fun trackEvents(event: String) {
        analytics.trackEvent(
            BASE_EVENT,
            Bundle().apply { putString("${BASE_EVENT}Action", event) }
        )
        analytics.trackEvent("$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Banner"
    }
}
