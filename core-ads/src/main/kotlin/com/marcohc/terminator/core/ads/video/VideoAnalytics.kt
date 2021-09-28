package com.marcohc.terminator.core.ads.video

import android.os.Bundle
import com.marcohc.terminator.core.ads.video.VideoEvent.*
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface VideoAnalytics {
    fun trackEvent(event: VideoEvent): Completable
    fun trackClick(): Completable
}

internal class VideoAnalyticsImpl(
    private val analytics: Analytics,
    private val scopeId: String
) : VideoAnalytics {

    override fun trackEvent(event: VideoEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> trackEvents("Available")
            is FailedToLoad -> trackEvents("NotAvailable")
            is Opened -> trackEvents("Opened")
            is Rewarded -> trackEvents("Rewarded")
            is Closed -> trackEvents("Closed")
            else -> {
                // No-op
            }
        }
    }

    override fun trackClick() = Completable.fromAction { trackEvents("Click") }

    private fun trackEvents(event: String) {
        analytics.trackEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}Action", "$scopeId$event") })
        analytics.trackEvent("$scopeId$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Video"
    }
}
