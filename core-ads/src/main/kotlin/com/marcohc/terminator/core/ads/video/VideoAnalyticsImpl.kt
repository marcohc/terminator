package com.marcohc.terminator.core.ads.video

import android.os.Bundle
import com.marcohc.terminator.core.ads.video.VideoEvent.Closed
import com.marcohc.terminator.core.ads.video.VideoEvent.FailedToLoad
import com.marcohc.terminator.core.ads.video.VideoEvent.Loaded
import com.marcohc.terminator.core.ads.video.VideoEvent.Opened
import com.marcohc.terminator.core.ads.video.VideoEvent.Rewarded
import com.marcohc.terminator.core.ads.video.VideoEvent.RewardedFailedToLoad
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

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
            is RewardedFailedToLoad -> logEvents("rewarded_failed_to_load")
            is Closed -> logEvents("closed")
        }
    }

    override fun logClick() = Completable.fromAction {
        analytics.logClick(scopeId, "video_click")
        logEvents("click")
    }

    private fun logEvents(parameter: String) {
        analytics.logEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}_action", "${scopeId}_${parameter}") })
        analytics.logEvent("${scopeId}_${BASE_EVENT}_${parameter}")
    }
    private companion object {
        const val BASE_EVENT = "video"
    }
}
