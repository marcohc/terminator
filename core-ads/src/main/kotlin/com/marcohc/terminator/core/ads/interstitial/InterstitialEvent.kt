package com.marcohc.terminator.core.ads.interstitial

sealed class InterstitialEvent {
    object NotLoadedYet : InterstitialEvent()
    object Loaded : InterstitialEvent()
    object FailedToLoad : InterstitialEvent()
    object Opened : InterstitialEvent()
    object Impression : InterstitialEvent()
    object Click : InterstitialEvent()
    object LeftApplication : InterstitialEvent()
    object Closed : InterstitialEvent()
}
