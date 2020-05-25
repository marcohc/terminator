package com.marcohc.terminator.core.ads.video

import android.app.Activity
import com.marcohc.terminator.core.ads.video.VideoRepository.Companion.factoryStubVideoRepository
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.scope.Scope

class VideoUseCase private constructor(
        private val repository: VideoRepository,
        private val analytics: VideoAnalytics
) {

    fun observeAndTrack(): Observable<VideoEvent> = repository.observe()
        .flatMap { event -> analytics.logEvent(event).toSingleDefault(event).toObservable() }

    fun logShowVideoClick() = analytics.logClick()

    fun show(activity: Activity) = repository.openVideo(activity)

    companion object {
        fun Scope.factoryVideoUseCase(scopeId: String) = VideoUseCase(
            repository = getScope(scopeId).get(),
            analytics = VideoAnalyticsImpl(
                analytics = get(),
                scopeId = scopeId
            )
        )

        fun factoryStubVideoUseCase() = VideoUseCase(
            repository = factoryStubVideoRepository(),
            analytics = object : VideoAnalytics {
                override fun logEvent(event: VideoEvent) = Completable.complete()
                override fun logClick() = Completable.complete()
            }
        )
    }
}
