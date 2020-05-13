package com.marcohc.terminator.core.ads.video

interface VideoAnalytics {
    fun logClick()
    fun logAvailable()
    fun logNotAvailable()
    fun logOpened()
    fun logRewarded()
    fun logRewardedFailedToLoad()
    fun logClosed()
}
