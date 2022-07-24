package com.marcohc.terminator.core.ads.banner

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.android.gms.ads.*
import com.marcohc.terminator.core.ads.AdsConstants
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface BannerUseCase {

    @MainThread
    fun load(context: Context)

    fun observe(): Observable<BannerStatus>

    companion object {

        fun Scope.factoryBannerUseCase(): BannerUseCase = BannerUseCaseImpl(
            analytics = BannerAnalytics(analytics = get()),
            adUnitId = get(named(AdsConstants.BANNER_ADS_UNIT_ID))
        )

        fun factoryStubBannerUseCase() = object : BannerUseCase {
            override fun load(context: Context) {}
            override fun observe() = Observable.never<BannerStatus>()
        }
    }
}

internal class BannerUseCaseImpl(
    private val analytics: BannerAnalytics,
    private val adUnitId: String
) : BannerUseCase,
    DefaultLifecycleObserver {

    private val subject = BehaviorSubject.createDefault<BannerStatus>(BannerStatus.Loading)
    private lateinit var adView: AdView

    @MainThread
    override fun load(context: Context) {
        adView = AdView(context)
        adView.adUnitId = adUnitId
        adView.setAdSize(AdSize.LARGE_BANNER)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Timber.v("BannerEvent.Loaded")
                analytics.trackEvent(BannerEvent.Loaded(adView))
                subject.onNext(BannerStatus.Available(adView))
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Timber.v("BannerEvent.FailedToLoad: $loadAdError")
                analytics.trackEvent(BannerEvent.FailedToLoad)
                subject.onNext(BannerStatus.NotAvailable)
            }

            override fun onAdOpened() {
                Timber.v("BannerEvent.Opened")
                analytics.trackEvent(BannerEvent.Opened)
            }

            override fun onAdImpression() {
                Timber.v("BannerEvent.onAdImpression")
                analytics.trackEvent(BannerEvent.Impression)
            }

            override fun onAdClosed() {
                Timber.v("BannerEvent.Closed")
                analytics.trackEvent(BannerEvent.Closed)
                subject.onNext(BannerStatus.Closed)
                loadAd()
            }

            override fun onAdClicked() {
                Timber.v("BannerEvent.Click")
                analytics.trackEvent(BannerEvent.Click)
            }
        }

        loadAd()
    }

    override fun observe(): Observable<BannerStatus> = subject.hide()

    private fun loadAd() {
        adView.loadAd(AdRequest.Builder().build())
    }
}
