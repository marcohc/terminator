package com.marcohc.terminator.core.ads.video

import android.app.Activity
import android.content.Context
import androidx.annotation.MainThread
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.marcohc.terminator.core.ads.AdsConstants
import com.marcohc.terminator.core.ads.AdsModule
import com.marcohc.terminator.core.mvi.ext.getOrCreateFromParentScope
import com.marcohc.terminator.core.utils.toObservableDefault
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface VideoUseCase {

    fun observe(): Observable<VideoEvent>

    fun observeAndTrack(): Observable<VideoEvent>

    fun getLastEvent(): VideoEvent

    fun logShowVideoClick(): Completable

    @MainThread
    fun show(activity: Activity): Completable

    companion object {

        fun Scope.getOrCreateScopedVideoUseCase(
                analyticsScopeId: String,
                activity: Activity
        ): VideoUseCase = getOrCreateFromParentScope(AdsModule.scopeId) { factoryVideoUseCase(analyticsScopeId, activity) }

        fun Scope.factoryVideoUseCase(
                analyticsScopeId: String,
                activity: Activity
        ): VideoUseCase = VideoUseCaseImpl(
            context = activity,
            adUnitId = get(named(AdsConstants.VIDEO_ADS_UNIT_ID)),
            analytics = VideoAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            )
        )

        fun factoryStubVideoUseCase(): VideoUseCase = object : VideoUseCase {
            override fun observe() = Observable.never<VideoEvent>()
            override fun observeAndTrack() = Observable.never<VideoEvent>()
            override fun getLastEvent(): VideoEvent = VideoEvent.NotLoadedYet
            override fun logShowVideoClick() = Completable.complete()
            override fun show(activity: Activity) = Completable.complete()
        }
    }
}

internal class VideoUseCaseImpl(
        private val context: Context,
        private val adUnitId: String,
        private val analytics: VideoAnalytics
) : VideoUseCase {

    private val subject = BehaviorSubject.createDefault<VideoEvent>(VideoEvent.NotLoadedYet)
    private lateinit var rewardedAd: RewardedAd

    init {
        loadAd()
        Timber.v("rewardedVideoAd: $context / $rewardedAd")
    }

    override fun observe(): Observable<VideoEvent> = subject.hide()

    override fun observeAndTrack(): Observable<VideoEvent> = observe().flatMap { analytics.logEvent(it).toObservableDefault(it) }

    override fun getLastEvent() = requireNotNull(subject.value) { "This subject must contain always a value" }

    override fun logShowVideoClick() = analytics.logClick()

    @MainThread
    override fun show(activity: Activity) = Completable
        .fromAction {
            Timber.v("VideoEvent: open video")
            if (rewardedAd.isLoaded) {
                Timber.v("VideoEvent: ad is loaded, show it!")
                rewardedAd.show(activity, object : RewardedAdCallback() {
                    override fun onRewardedAdOpened() {
                        Timber.v("VideoEvent.Opened")
                        subject.onNext(VideoEvent.Opened)
                    }

                    override fun onRewardedAdClosed() {
                        Timber.v("VideoEvent.Closed")
                        subject.onNext(VideoEvent.Closed)
                        subject.onNext(VideoEvent.NotLoadedYet)
                        loadAd()
                    }

                    override fun onUserEarnedReward(rewardItem: RewardItem) {
                        Timber.v("VideoEvent.Rewarded: ${rewardItem.type} / ${rewardItem.amount}")
                        subject.onNext(VideoEvent.Rewarded(rewardItem.type, rewardItem.amount))
                    }

                    override fun onRewardedAdFailedToShow(errorCode: Int) {
                        when (errorCode) {
                            ERROR_CODE_INTERNAL_ERROR -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_INTERNAL_ERROR")
                            ERROR_CODE_AD_REUSED -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_AD_REUSED")
                            ERROR_CODE_NOT_READY -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_NOT_READY")
                            ERROR_CODE_APP_NOT_FOREGROUND -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_APP_NOT_FOREGROUND")
                        }
                        subject.onNext(VideoEvent.RewardedFailedToLoad)
                    }
                })
            }
        }

    private fun loadAd() {
        val builder = AdRequest.Builder()
        rewardedAd = RewardedAd(context, adUnitId)
        rewardedAd.loadAd(
            builder.build(),
            object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    Timber.v("VideoEvent.Loaded")
                    subject.onNext(VideoEvent.Loaded)
                }

                override fun onRewardedAdFailedToLoad(errorCode: Int) {
                    when (errorCode) {
                        AdRequest.ERROR_CODE_INTERNAL_ERROR -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_INTERNAL_ERROR")
                        AdRequest.ERROR_CODE_INVALID_REQUEST -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_INVALID_REQUEST")
                        AdRequest.ERROR_CODE_NETWORK_ERROR -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_NETWORK_ERROR")
                        AdRequest.ERROR_CODE_NO_FILL -> Timber.v("VideoEvent.FailedToLoad: ERROR_CODE_NO_FILL")
                    }
                    subject.onNext(VideoEvent.FailedToLoad)
                }
            })
    }

}
