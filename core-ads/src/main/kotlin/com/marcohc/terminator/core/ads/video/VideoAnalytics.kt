package com.marcohc.terminator.core.ads.video

import android.os.Bundle
import com.marcohc.terminator.core.ads.video.VideoEvent.Closed
import com.marcohc.terminator.core.ads.video.VideoEvent.FailedToLoad
import com.marcohc.terminator.core.ads.video.VideoEvent.Loaded
import com.marcohc.terminator.core.ads.video.VideoEvent.Opened
import com.marcohc.terminator.core.ads.video.VideoEvent.Rewarded
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface VideoAnalytics {
    fun logEvent(event: VideoEvent): Completable

    fun logClick(): Completable
}

internal class VideoAnalyticsImpl(
        private val analytics: Analytics,
        private val scopeId: String
) : VideoAnalytics {

    override fun logEvent(event: VideoEvent) = Completable.fromAction {
        when (event) {
            is Loaded -> logEvents("available")
            is FailedToLoad -> logEvents("not_available")
            is Opened -> logEvents("opened")
            is Rewarded -> logEvents("rewarded")
            is Closed -> logEvents("closed")
            else -> {
                // No-op
            }
        }
    }

    override fun logClick() = Completable.fromAction {
        analytics.trackClick(scopeId, "${BASE_EVENT}_click")
        logEvents("click")
    }

    private fun logEvents(parameter: String) {
        analytics.trackEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}_action", "${scopeId}_${parameter}") })
        analytics.trackEvent("${scopeId}_${BASE_EVENT}_${parameter}")
    }
    private companion object {
        const val BASE_EVENT = "video"
    }
}
