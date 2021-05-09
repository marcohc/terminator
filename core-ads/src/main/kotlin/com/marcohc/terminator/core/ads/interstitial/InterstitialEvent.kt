package com.marcohc.terminator.core.ads.interstitial

sealed class InterstitialEvent {
    object NotLoadedYet : InterstitialEvent()
    object Loaded : InterstitialEvent()
    object FailedToLoad : InterstitialEvent()
    object Opened : InterstitialEvent()
    object Closed : InterstitialEvent()
}
