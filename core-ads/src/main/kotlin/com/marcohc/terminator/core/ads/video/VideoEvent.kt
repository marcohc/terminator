package com.marcohc.terminator.core.ads.video

sealed class VideoEvent {
    object NotLoadedYet : VideoEvent()
    object Loaded : VideoEvent()
    object FailedToLoad : VideoEvent()
    object Opened : VideoEvent()
    data class Rewarded(
            val type: String?,
            val amount: Int?
    ) : VideoEvent()
    object Closed : VideoEvent()
}
