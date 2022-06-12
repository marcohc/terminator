package com.marcohc.terminator.core.ads.interstitial

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
        ) = getOrCreateFromParentScope(AdsModule.scopeId) {
            factoryInterstitialUseCase(
                analyticsScopeId,
                activity
            )
        }

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
    DefaultLifecycleObserver {

    private val subject =
        BehaviorSubject.createDefault<InterstitialEvent>(InterstitialEvent.NotLoadedYet)
    private var interstitialAd: InterstitialAd? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {

        MobileAds.initialize(activity)

        InterstitialAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.v("InterstitialEvent.onAdFailedToLoad: $loadAdError")
                    subject.onNext(InterstitialEvent.FailedToLoad)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    this@InterstitialUseCaseImpl.interstitialAd = interstitialAd

                    this@InterstitialUseCaseImpl.interstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                // No-op
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Timber.v("InterstitialEvent.onAdDismissedFullScreenContent")
                                subject.onNext(InterstitialEvent.Closed)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Timber.v("InterstitialEvent.onAdFailedToShowFullScreenContent: $adError")
                                subject.onNext(InterstitialEvent.FailedToLoad)
                            }

                            override fun onAdImpression() {
                                // No-op
                            }

                            override fun onAdShowedFullScreenContent() {
                                Timber.v("InterstitialEvent.onAdShowedFullScreenContent")
                                subject.onNext(InterstitialEvent.Opened)
                                this@InterstitialUseCaseImpl.interstitialAd = null
                            }
                        }

                    Timber.v("InterstitialEvent.onAdLoaded")
                    subject.onNext(InterstitialEvent.Loaded)
                }
            }
        )
    }

    override fun observe(): Observable<InterstitialEvent> = subject.hide()

    override fun observeAndTrack(): Observable<InterstitialEvent> =
        observe().flatMap { analytics.trackEvent(it).toObservableDefault(it) }

    override fun getLastEvent() =
        requireNotNull(subject.value) { "This subject must contain always a value" }

    @MainThread
    override fun show() = Completable.fromAction {
        interstitialAd?.show(activity)
    }
}
