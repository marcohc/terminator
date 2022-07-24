package com.marcohc.terminator.core.ads.banner

import com.google.android.gms.ads.AdView

sealed class BannerStatus {
    object Loading : BannerStatus()
    object NotAvailable : BannerStatus()
    data class Available(val adView: AdView) : BannerStatus()
    object Closed : BannerStatus()
}
