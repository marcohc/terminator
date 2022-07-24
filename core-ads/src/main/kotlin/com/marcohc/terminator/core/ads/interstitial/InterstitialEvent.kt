package com.marcohc.terminator.core.ads.interstitial

import com.google.android.gms.ads.AdValue

internal sealed class InterstitialEvent {
    object Loading : InterstitialEvent()
    object Loaded : InterstitialEvent()
    object FailedToLoad : InterstitialEvent()
    object Opened : InterstitialEvent()
    object Impression : InterstitialEvent()
    object Clicked : InterstitialEvent()
    data class Paid(
        val valueMicros: Long,
        val currencyCode: String,
        val precisionType: Int,
    ) : InterstitialEvent()
    object Closed : InterstitialEvent()
}
