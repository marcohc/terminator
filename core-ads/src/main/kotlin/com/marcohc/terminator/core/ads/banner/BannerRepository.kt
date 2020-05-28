package com.marcohc.terminator.core.ads.banner

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.marcohc.terminator.core.ads.AdsConstants
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface BannerRepository {

    fun observe(): Observable<BannerEvent>

    companion object {
        fun Scope.factoryBannerRepository(activity: AppCompatActivity): BannerRepository = BannerRepositoryImpl(
            activity = activity,
            adUnitId = get(named(AdsConstants.BANNER_ADS_UNIT_ID))
        )

        fun factoryStubBannerRepository() = object : BannerRepository {
            override fun observe() = Observable.never<BannerEvent>()
        }
    }
}

internal class BannerRepositoryImpl(
        private val activity: AppCompatActivity,
        private val adUnitId: String
) : BannerRepository,
    LifecycleObserver {

    private val subject = BehaviorSubject.createDefault<BannerEvent>(BannerEvent.NotLoadedYet)
    private lateinit var adView: AdView

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        MobileAds.initialize(activity) {}

        adView = AdView(activity)
        adView.adUnitId = adUnitId
        adView.adSize = AdSize.LARGE_BANNER

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Timber.v("BannerEvent.onAdLoaded")
                subject.onNext(BannerEvent.Loaded(adView))
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Timber.v("BannerEvent.FailedToLoad: $errorCode")
                subject.onNext(BannerEvent.FailedToLoad)
            }

            override fun onAdOpened() {
                Timber.v("BannerEvent.Opened")
                subject.onNext(BannerEvent.Opened)
            }

            override fun onAdImpression() {
                Timber.v("BannerEvent.onAdImpression")
                subject.onNext(BannerEvent.Impression)
            }

            override fun onAdClosed() {
                Timber.v("BannerEvent.Closed")
                subject.onNext(BannerEvent.Closed)
            }

            override fun onAdClicked() {
                Timber.v("BannerEvent.Click")
                subject.onNext(BannerEvent.Click)
            }

            override fun onAdLeftApplication() {
                Timber.v("BannerEvent.LeftApplication")
                subject.onNext(BannerEvent.LeftApplication)
            }
        }

        // Custom values could be added here
        val builder = AdRequest.Builder()

        adView.loadAd(builder.build())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        adView.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        adView.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        adView.destroy()
    }

    override fun observe(): Observable<BannerEvent> = subject.hide()

}
