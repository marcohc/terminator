package com.marcohc.terminator.core.ads.video

import android.os.Bundle
import com.marcohc.terminator.core.ads.video.VideoEvent.*
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

internal class VideoAnalytics(
    private val analytics: Analytics
) {

    fun trackEvent(event: VideoEvent) = Completable.fromAction {
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

    fun trackClick() = Completable.fromAction { trackEvents("Click") }

    private fun trackEvents(event: String) {
        analytics.trackEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}Action", event) })
        analytics.trackEvent("$BASE_EVENT${event}")
    }

    private companion object {
        const val BASE_EVENT = "Video"
    }
}
