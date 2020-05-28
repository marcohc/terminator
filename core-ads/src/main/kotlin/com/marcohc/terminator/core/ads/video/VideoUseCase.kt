package com.marcohc.terminator.core.ads.video

import android.app.Activity
import androidx.annotation.MainThread
import com.marcohc.terminator.core.ads.video.VideoRepository.Companion.factoryStubVideoRepository
import com.marcohc.terminator.core.ads.video.VideoRepository.Companion.factoryVideoRepository
import com.marcohc.terminator.core.mvi.ext.fetchOrCreateFromParentScope
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

    fun getLastEvent() = repository.getLastEvent()

    @MainThread
    fun show(activity: Activity) = repository.show(activity)

    companion object {
        fun Scope.factoryVideoUseCase(
                libraryScopeId: String,
                analyticsScopeId: String,
                activity: Activity
        ) = VideoUseCase(
            repository = fetchOrCreateFromParentScope(libraryScopeId) {
                factoryVideoRepository(activity)
            },
            analytics = VideoAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
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
