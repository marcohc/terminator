package com.marcohc.terminator.core.ads.banner

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.*
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

interface BannerUseCase {

    fun observe(): Observable<BannerEvent>

    fun observeAndTrack(): Observable<BannerEvent>

    fun getLastEvent(): BannerEvent

    @MainThread
    fun loadNewAd(): Completable

    companion object {

        fun Scope.getOrCreateScopedBannerUseCase(
            analyticsScopeId: String,
            activity: AppCompatActivity
        ): BannerUseCase = getOrCreateFromParentScope(AdsModule.scopeId) {
            factoryBannerUseCase(
                analyticsScopeId,
                activity
            )
        }

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
            override fun getLastEvent() = BannerEvent.NotLoadedYet
            override fun loadNewAd() = Completable.complete()
        }
    }
}

internal class BannerUseCaseImpl(
    private val activity: AppCompatActivity,
    private val analytics: BannerAnalytics,
    private val adUnitId: String
) : BannerUseCase,
    DefaultLifecycleObserver {

    private val subject = BehaviorSubject.createDefault<BannerEvent>(BannerEvent.NotLoadedYet)
    private lateinit var adView: AdView

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {

        MobileAds.initialize(activity) {}

        adView = AdView(activity)
        adView.adUnitId = adUnitId
        adView.adSize = AdSize.LARGE_BANNER

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Timber.v("BannerEvent.onAdLoaded")
                subject.onNext(BannerEvent.Loaded(adView))
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Timber.v("BannerEvent.FailedToLoad: $loadAdError")
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
                loadAd()
            }

            override fun onAdClicked() {
                Timber.v("BannerEvent.Click")
                subject.onNext(BannerEvent.Click)
            }
        }

        loadAd()
    }

    override fun onResume(owner: LifecycleOwner) {
        adView.resume()
    }

    override fun onPause(owner: LifecycleOwner) {
        adView.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        adView.destroy()
    }

    override fun observe(): Observable<BannerEvent> = subject.hide()

    override fun observeAndTrack(): Observable<BannerEvent> = observe()
        .flatMap { event -> analytics.trackEvent(event).toObservableDefault(event) }

    override fun getLastEvent() =
        requireNotNull(subject.value) { "This subject must contain always a value" }

    @MainThread
    override fun loadNewAd() = Completable.fromAction { loadAd() }

    private fun loadAd() {
        adView.loadAd(AdRequest.Builder().build())
    }
}
