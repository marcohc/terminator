package com.marcohc.terminator.core.ads.native

sealed class NativeEvent {
    object NotLoadedYet : NativeEvent()
    object Loaded : NativeEvent()
    object FailedToLoad : NativeEvent()
    object Opened : NativeEvent()
    object Impression : NativeEvent()
    object Click : NativeEvent()
    object LeftApplication : NativeEvent()
    object Closed : NativeEvent()
}
