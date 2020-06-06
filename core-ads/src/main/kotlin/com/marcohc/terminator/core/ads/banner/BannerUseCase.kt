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
import com.marcohc.terminator.core.ads.AdsModule
import com.marcohc.terminator.core.mvi.ext.getOrCreateFromParentScope
import com.marcohc.terminator.core.utils.toObservableDefault
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface BannerUseCase {

    fun observe(): Observable<BannerEvent>

    fun observeAndTrack(): Observable<BannerEvent>

    companion object {

        fun Scope.getOrCreateScopedBannerUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ): BannerUseCase = getOrCreateFromParentScope(AdsModule.scopeId) { factoryBannerUseCase(analyticsScopeId, activity) }

        fun Scope.factoryBannerUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ): BannerUseCase = BannerUseCaseImpl(
            activity = activity,
            analytics = BannerAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            ),
            adUnitId = get(named(AdsConstants.BANNER_ADS_UNIT_ID))
        )

        fun factoryStubBannerUseCase() = object : BannerUseCase {
            override fun observe() = Observable.never<BannerEvent>()
            override fun observeAndTrack() = Observable.never<BannerEvent>()
        }
    }

}

internal class BannerUseCaseImpl(
        private val activity: AppCompatActivity,
        private val analytics: BannerAnalytics,
        private val adUnitId: String
) : BannerUseCase,
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

    override fun observeAndTrack(): Observable<BannerEvent> = observe()
        .flatMap { event -> analytics.logEvent(event).toObservableDefault(event) }

}
