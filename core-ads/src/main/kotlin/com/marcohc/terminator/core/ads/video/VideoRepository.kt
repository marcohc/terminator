package com.marcohc.terminator.core.ads.video

import android.app.Activity
import androidx.annotation.MainThread
import com.marcohc.terminator.core.ads.AdsConstants
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

interface VideoRepository {

    @MainThread
    fun loadVideo(): Completable

    fun observe(): Observable<VideoEvent>

    fun getLastEvent(): VideoEvent

    @MainThread
    fun openVideo(activity: Activity): Completable

    companion object {
        fun Scope.factoryVideoRepository(activity: Activity): VideoRepository = VideoRepositoryImpl(
            context = activity,
            adUnitId = get(named(AdsConstants.VIDEO_ADS_UNIT_ID))
        )

        fun factoryStubVideoRepository(): VideoRepository = object : VideoRepository {
            override fun loadVideo() = Completable.complete()
            override fun observe() = Observable.never<VideoEvent>()
            override fun getLastEvent(): VideoEvent = VideoEvent.NotLoadedYet
            override fun openVideo(activity: Activity) = Completable.complete()
        }
    }
}
