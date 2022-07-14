package com.marcohc.terminator.core.ads.video

import android.app.Activity
import android.content.Context
import androidx.annotation.MainThread
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.marcohc.terminator.core.ads.AdsConstants
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface VideoUseCase {

    @MainThread
    fun load(context: Context)

    @MainThread
    fun show(activity: Activity): Single<Reward>

    fun observe(): Observable<VideoStatus>

    fun isLoaded(): Boolean

    fun logShowVideoClick(): Completable

    companion object {

        fun Scope.factoryVideoUseCase(): VideoUseCase = VideoUseCaseImpl(
            adUnitId = get(named(AdsConstants.VIDEO_ADS_UNIT_ID)),
            analytics = VideoAnalytics(analytics = get())
        )

        fun factoryStubVideoUseCase(): VideoUseCase = object : VideoUseCase {
            override fun load(context: Context) {}
            override fun observe() = Observable.never<VideoStatus>()
            override fun isLoaded() = false
            override fun logShowVideoClick() = Completable.complete()
            override fun show(activity: Activity) = Single.never<Reward>()
        }
    }
}

internal class VideoUseCaseImpl(
    private val adUnitId: String,
    private val analytics: VideoAnalytics
) : VideoUseCase {

    private val subject = BehaviorSubject.createDefault<VideoStatus>(VideoStatus.Loading)
    private var rewardedAd: RewardedAd? = null

    @MainThread
    override fun load(context: Context) {
        if (!isLoaded()) {

            Timber.v("VideoEvent.Loading")
            analytics.trackEvent(VideoEvent.Loading)
            subject.onNext(VideoStatus.Loading)

            RewardedAd.load(
                context,
                adUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Timber.v("VideoEvent.FailedToLoad: $loadAdError")
                        analytics.trackEvent(VideoEvent.FailedToLoad)
                        subject.onNext(VideoStatus.NotAvailable)
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        this@VideoUseCaseImpl.rewardedAd = rewardedAd

                        this@VideoUseCaseImpl.rewardedAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    Timber.v("VideoEvent.Closed")
                                    analytics.trackEvent(VideoEvent.Closed)
                                    subject.onNext(VideoStatus.NotAvailable)
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    Timber.v("VideoEvent.FailedToLoad: $adError")
                                    analytics.trackEvent(VideoEvent.FailedToLoad)
                                    subject.onNext(VideoStatus.NotAvailable)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    Timber.v("VideoEvent.Opened")
                                    analytics.trackEvent(VideoEvent.Opened)
                                    this@VideoUseCaseImpl.rewardedAd = null
                                }
                            }

                        Timber.v("VideoEvent.Loaded: $rewardedAd")
                        analytics.trackEvent(VideoEvent.Loaded)
                        subject.onNext(VideoStatus.Available)
                    }
                }
            )
        }
    }

    @MainThread
    override fun show(activity: Activity) = Single.create<Reward> { emitter ->
        Timber.v("show: open video")
        if (!isLoaded()) {
            emitter.onError(IllegalStateException("Video is not loaded!"))
        }
        rewardedAd?.show(activity) { rewardItem ->
            Timber.v("VideoEvent.Rewarded: ${rewardItem.type} / ${rewardItem.amount}")
            val reward = Reward(rewardItem.type, rewardItem.amount)
            analytics.trackEvent(VideoEvent.Rewarded(rewardItem.type, rewardItem.amount))
            subject.onNext(VideoStatus.NotAvailable)
            emitter.onSuccess(reward)
        }
    }

    override fun observe(): Observable<VideoStatus> = subject.hide()

    override fun isLoaded(): Boolean = rewardedAd != null

    override fun logShowVideoClick() = analytics.trackClick()

}
