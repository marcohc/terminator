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
            is Loaded -> logEvent("available")
            is FailedToLoad -> logEvent("not_available")
            is Opened -> logEvent("opened")
            is Rewarded -> logEvent("rewarded")
            is RewardedFailedToLoad -> logEvent("rewarded_failed_to_load")
            is Closed -> logEvent("closed")
        }
    }

    override fun logClick() = Completable.fromAction {
        analytics.logClick(scopeId, "video_click")
        logEvent("click")
    }

    private fun logEvent(parameter: String) {
        val parameterKey = "${scopeId}_${parameter}"
        analytics.logCustomEvent("video", Bundle().apply { putString("video_action", parameterKey) })
    }
}
