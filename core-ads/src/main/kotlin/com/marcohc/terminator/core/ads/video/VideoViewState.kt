package com.marcohc.terminator.core.ads.video

sealed class VideoViewState {
    object Loading : VideoViewState()
    object VideoNotAvailable : VideoViewState()
    data class VideoAvailable(val earnTimeText: String) : VideoViewState()
    data class Completed(val rewardText: String) : VideoViewState()
}
