package com.marcohc.terminator.core.ads.video

sealed class VideoStatus {
    object Loading : VideoStatus()
    object NotAvailable : VideoStatus()
    object Available : VideoStatus()
}
