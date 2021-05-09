package com.marcohc.terminator.core.ads.banner

import com.google.android.gms.ads.AdView

sealed class BannerEvent {
    object NotLoadedYet : BannerEvent()
    data class Loaded(val adView: AdView) : BannerEvent()
    object FailedToLoad : BannerEvent()
    object Opened : BannerEvent()
    object Impression : BannerEvent()
    object Click : BannerEvent()
    object Closed : BannerEvent()
}
