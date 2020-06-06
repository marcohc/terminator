package com.marcohc.terminator.core.ads.interstitial

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
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

interface InterstitialUseCase {

    fun observe(): Observable<InterstitialEvent>

    fun observeAndTrack(): Observable<InterstitialEvent>

    fun getLastEvent(): InterstitialEvent

    @MainThread
    fun show(): Completable

    companion object {

        fun Scope.getOrCreateScopedInterstitialUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ) = getOrCreateFromParentScope(AdsModule.scopeId) { factoryInterstitialUseCase(analyticsScopeId, activity) }

        fun Scope.factoryInterstitialUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ): InterstitialUseCase = InterstitialUseCaseImpl(
            activity = activity,
            analytics = InterstitialAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            ),
            adUnitId = get(named(AdsConstants.INTERSTITIAL_ADS_UNIT_ID))
        )

        fun factoryStubInterstitialUseCase() = object : InterstitialUseCase {
            override fun observe() = Observable.never<InterstitialEvent>()
            override fun observeAndTrack() = Observable.never<InterstitialEvent>()
            override fun getLastEvent() = InterstitialEvent.NotLoadedYet
            override fun show() = Completable.complete()
        }
    }

}

internal class InterstitialUseCaseImpl(
        private val activity: AppCompatActivity,
        private val analytics: InterstitialAnalytics,
        private val adUnitId: String
) : InterstitialUseCase,
    LifecycleObserver {

    private val subject = BehaviorSubject.createDefault<InterstitialEvent>(InterstitialEvent.NotLoadedYet)
    private lateinit var interstitialAd: InterstitialAd

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        MobileAds.initialize(activity) {}

        interstitialAd = InterstitialAd(activity)
        interstitialAd.adUnitId = adUnitId

        interstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Timber.v("InterstitialEvent.onAdLoaded")
                subject.onNext(InterstitialEvent.Loaded)
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Timber.v("InterstitialEvent.FailedToLoad: $errorCode")
                subject.onNext(InterstitialEvent.FailedToLoad)
            }

            override fun onAdOpened() {
                Timber.v("InterstitialEvent.Opened")
                subject.onNext(InterstitialEvent.Opened)
            }

            override fun onAdImpression() {
                Timber.v("InterstitialEvent.onAdImpression")
                subject.onNext(InterstitialEvent.Impression)
            }

            override fun onAdClosed() {
                Timber.v("InterstitialEvent.Closed")
                subject.onNext(InterstitialEvent.Closed)
                loadNewAd()
            }

            override fun onAdClicked() {
                Timber.v("InterstitialEvent.Click")
                subject.onNext(InterstitialEvent.Click)
            }

            override fun onAdLeftApplication() {
                Timber.v("InterstitialEvent.LeftApplication")
                subject.onNext(InterstitialEvent.LeftApplication)
            }
        }

        loadNewAd()
    }

    override fun observe(): Observable<InterstitialEvent> = subject.hide()

    override fun observeAndTrack(): Observable<InterstitialEvent> = observe().flatMap { analytics.logEvent(it).toObservableDefault(it) }

    override fun getLastEvent() = requireNotNull(subject.value) { "This subject must contain always a value" }

    override fun show() = Completable.fromAction {
        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        }
    }

    private fun loadNewAd() {
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

}
