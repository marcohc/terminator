package com.marcohc.terminator.core.ads.banner

import android.os.Bundle
import com.marcohc.terminator.core.ads.banner.BannerEvent.Click
import com.marcohc.terminator.core.ads.banner.BannerEvent.Closed
import com.marcohc.terminator.core.ads.banner.BannerEvent.FailedToLoad
import com.marcohc.terminator.core.ads.banner.BannerEvent.Impression
import com.marcohc.terminator.core.ads.banner.BannerEvent.Loaded
import com.marcohc.terminator.core.ads.banner.BannerEvent.Opened
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface BannerAnalytics {
    fun trackEvent(event: BannerEvent): Completable
}

internal class BannerAnalyticsImpl(
    private val analytics: Analytics,
    private val scopeId: String
) : BannerAnalytics {

    override fun trackEvent(event: BannerEvent) = Completable.fromAction {
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
        analytics.trackEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}Action", "$scopeId$event") })
        analytics.trackEvent("$scopeId$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Banner"
    }
}
