package com.marcohc.terminator.core.ads.video

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

internal class VideoRepositoryImpl(
        private val activity: AppCompatActivity,
        private val adUnitId: String
) : VideoRepository,
    LifecycleObserver {

    private val subject = BehaviorSubject.createDefault<VideoEvent>(VideoEvent.NotLoadedYet)
    private lateinit var rewardedAd: RewardedAd

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        rewardedAd = RewardedAd(activity, adUnitId)
        Timber.v("rewardedVideoAd: $activity / $rewardedAd")

        loadAd()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        activity.lifecycle.removeObserver(this)
    }

    override fun observe(): Observable<VideoEvent> = subject.hide()

    override fun getLastEvent() = requireNotNull(subject.value) { "This subject must contain always a value" }

    @MainThread
    override fun openVideo() = Completable
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

    @MainThread
    override fun loadVideo() = Completable.fromAction { loadAd() }

    private fun loadAd() {
        val builder = AdRequest.Builder()
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
