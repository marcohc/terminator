package com.marcohc.terminator.core.ads.video

import io.reactivex.Completable

interface VideoAnalytics {
    fun logEvent(event: VideoEvent): Completable

    fun logClick(): Completable
}
