package com.marcohc.terminator.core.ads.video

internal sealed class VideoEvent {
    object Loading : VideoEvent()
    object Loaded : VideoEvent()
    object FailedToLoad : VideoEvent()
    object Opened : VideoEvent()
    data class Rewarded(
        val type: String?,
        val amount: Int?
    ) : VideoEvent()

    object Closed : VideoEvent()
}

data class Reward(
    val type: String?,
    val amount: Int?
)
