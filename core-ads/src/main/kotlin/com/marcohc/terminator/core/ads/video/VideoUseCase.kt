package com.marcohc.terminator.core.ads.video

import android.app.Activity
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
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
            activity: AppCompatActivity
        ): VideoUseCase = getOrCreateFromParentScope(AdsModule.scopeId) {
            factoryVideoUseCase(
                analyticsScopeId,
                activity
            )
        }

        fun Scope.factoryVideoUseCase(
            analyticsScopeId: String,
            activity: AppCompatActivity
        ): VideoUseCase = VideoUseCaseImpl(
            activity = activity,
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
    private val activity: AppCompatActivity,
    private val adUnitId: String,
    private val analytics: VideoAnalytics
) : VideoUseCase,
    LifecycleObserver {

    private val subject = BehaviorSubject.createDefault<VideoEvent>(VideoEvent.NotLoadedYet)
    private var rewardedAd: RewardedAd? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        MobileAds.initialize(activity)

        RewardedAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.v("VideoEvent.onAdFailedToLoad: $loadAdError")
                    subject.onNext(VideoEvent.FailedToLoad)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    this@VideoUseCaseImpl.rewardedAd = rewardedAd

                    this@VideoUseCaseImpl.rewardedAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Timber.v("VideoEvent.onAdDismissedFullScreenContent")
                                subject.onNext(VideoEvent.Closed)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                                Timber.v("VideoEvent.onAdFailedToShowFullScreenContent: $adError")
                                subject.onNext(VideoEvent.FailedToLoad)
                            }

                            override fun onAdShowedFullScreenContent() {
                                Timber.v("VideoEvent.onAdShowedFullScreenContent")
                                subject.onNext(VideoEvent.Opened)
                                this@VideoUseCaseImpl.rewardedAd = null
                            }
                        }

                    Timber.v("rewardedVideoAd: $activity / $rewardedAd")
                    Timber.v("VideoEvent.onAdLoaded")
                    subject.onNext(VideoEvent.Loaded)
                }
            }
        )
    }

    override fun observe(): Observable<VideoEvent> = subject.hide()

    override fun observeAndTrack(): Observable<VideoEvent> = observe()
        .flatMap { analytics.trackEvent(it).toObservableDefault(it) }

    override fun getLastEvent() = requireNotNull(subject.value) {
        "This subject must contain always a value"
    }

    override fun logShowVideoClick() = analytics.trackClick()

    @MainThread
    override fun show(activity: Activity) = Completable
        .fromAction {
            Timber.v("show: open video")
            rewardedAd?.show(activity) { rewardItem ->
                Timber.v("VideoEvent.Rewarded: ${rewardItem.type} / ${rewardItem.amount}")
                subject.onNext(VideoEvent.Rewarded(rewardItem.type, rewardItem.amount))
            }
        }
}
