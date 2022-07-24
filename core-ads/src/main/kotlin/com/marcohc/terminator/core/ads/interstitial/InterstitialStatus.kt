package com.marcohc.terminator.core.ads.interstitial

sealed class InterstitialStatus {
    object Loading : InterstitialStatus()
    object NotAvailable : InterstitialStatus()
    object Available : InterstitialStatus()
}
