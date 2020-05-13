package com.marcohc.terminator.core.ads.banner

import io.reactivex.Observable

interface BannerRepository {

    fun observe(): Observable<BannerEvent>

}
