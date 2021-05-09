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
    fun logEvent(event: BannerEvent): Completable
}

internal class BannerAnalyticsImpl(
        private val analytics: Analytics,
        private val scopeId: String
) : BannerAnalytics {

    override fun logEvent(event: BannerEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> logEvents("available")
            is FailedToLoad -> logEvents("not_available")
            is Opened -> logEvents("opened")
            is Impression -> logEvents("impression")
            is Click -> {
                analytics.logClick(scopeId, "${BASE_EVENT}_click")
                logEvents("click")
            }
            is Closed -> logEvents("closed")
        }
    }

    private fun logEvents(parameter: String) {
        analytics.logEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}_action", "${scopeId}_${parameter}") })
        analytics.logEvent("${scopeId}_${BASE_EVENT}_${parameter}")
    }

    private companion object {
        const val BASE_EVENT = "banner"
    }
}
