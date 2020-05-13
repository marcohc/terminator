package com.marcohc.terminator.core.ads.banner

import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.AdsConstants
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

object BannerRepositoryFactory : KoinComponent {

    fun newInstance(activity: AppCompatActivity): BannerRepository = BannerRepositoryImpl(
        activity = activity,
        applicationId = get(named(AdsConstants.BANNER_APPLICATION_ID)),
        adUnitId = get(named(AdsConstants.BANNER_ADS_UNIT_ID))
    )

}
