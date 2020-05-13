package com.marcohc.terminator.core.ads.video

import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.AdsConstants
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

object VideoRepositoryFactory : KoinComponent {

    fun newInstance(activity: AppCompatActivity): VideoRepository = VideoRepositoryImpl(
        activity = activity,
        adUnitId = get(named(AdsConstants.VIDEO_ADS_UNIT_ID))
    )

}
