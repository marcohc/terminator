package com.marcohc.terminator.core.ads.interstitial

import android.app.Activity
import android.content.Context
import androidx.annotation.MainThread
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.marcohc.terminator.core.ads.AdsConstants
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface InterstitialUseCase {

    fun load(context: Context)

    @MainThread
    fun show(activity: Activity): Single<Boolean>

    fun observe(): Observable<InterstitialStatus>

    fun isLoaded(): Boolean

    companion object {

        fun Scope.factoryInterstitialUseCase(): InterstitialUseCase = InterstitialUseCaseImpl(
            analytics = InterstitialAnalytics(analytics = get()),
            adUnitId = get(named(AdsConstants.INTERSTITIAL_ADS_UNIT_ID))
        )

        fun factoryStubInterstitialUseCase() = object : InterstitialUseCase {
            override fun load(context: Context) {}
            override fun observe() = Observable.never<InterstitialStatus>()
            override fun isLoaded(): Boolean = false
            override fun show(activity: Activity) = Single.just(false)
        }
    }
}

internal class InterstitialUseCaseImpl(
    private val analytics: InterstitialAnalytics,
    private val adUnitId: String
) : InterstitialUseCase {

    private val subject = BehaviorSubject.createDefault<InterstitialStatus>(
        InterstitialStatus.Loading
    )
    private var interstitialAd: InterstitialAd? = null

    @MainThread
    override fun load(context: Context) {
        if (!isLoaded()) {

            Timber.v("InterstitialStatus.Loading")
            analytics.trackEvent(InterstitialEvent.Loading)
            subject.onNext(InterstitialStatus.Loading)

            InterstitialAd.load(
                context,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Timber.v("InterstitialEvent.onAdFailedToLoad: $loadAdError")
                        analytics.trackEvent(InterstitialEvent.FailedToLoad)
                        subject.onNext(InterstitialStatus.NotAvailable)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        this@InterstitialUseCaseImpl.interstitialAd = interstitialAd

                        this@InterstitialUseCaseImpl.interstitialAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdClicked() {
                                    Timber.v("InterstitialEvent.Clicked")
                                    analytics.trackEvent(InterstitialEvent.Clicked)
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    Timber.v("InterstitialEvent.Closed")
                                    analytics.trackEvent(InterstitialEvent.Closed)
                                    subject.onNext(InterstitialStatus.NotAvailable)
                                    this@InterstitialUseCaseImpl.interstitialAd = null
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    Timber.v("InterstitialEvent.FailedToLoad: $adError")
                                    analytics.trackEvent(InterstitialEvent.FailedToLoad)
                                    subject.onNext(InterstitialStatus.NotAvailable)
                                    this@InterstitialUseCaseImpl.interstitialAd = null
                                }

                                override fun onAdImpression() {
                                    Timber.v("InterstitialEvent.Impression")
                                    analytics.trackEvent(InterstitialEvent.Impression)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    Timber.v("InterstitialEvent.Opened")
                                    analytics.trackEvent(InterstitialEvent.Opened)
                                    this@InterstitialUseCaseImpl.interstitialAd = null
                                }
                            }

                        Timber.v("InterstitialEvent.Loaded")
                        analytics.trackEvent(InterstitialEvent.Loaded)
                        subject.onNext(InterstitialStatus.Available)
                    }
                }
            )
        }
    }

    override fun observe(): Observable<InterstitialStatus> = subject.hide()

    override fun isLoaded() = interstitialAd != null

    @MainThread
    override fun show(activity: Activity) = Single.create<Boolean> { emitter ->
        Timber.v("show: open interstitial")
        if (!isLoaded()) {
            emitter.onError(IllegalStateException("Interstitial is not loaded!"))
        } else {
            requireNotNull(interstitialAd).run {
                setOnPaidEventListener { value ->
                    Timber.v(
                        "InterstitialEvent.Paid: " +
                                "${value.valueMicros} / ${value.currencyCode} / ${value.precisionType}"
                    )
                    analytics.trackEvent(
                        InterstitialEvent.Paid(
                            value.valueMicros,
                            value.currencyCode,
                            value.precisionType
                        )
                    )
                    emitter.onSuccess(true)
                }
                show(activity)
            }
        }
    }
}
