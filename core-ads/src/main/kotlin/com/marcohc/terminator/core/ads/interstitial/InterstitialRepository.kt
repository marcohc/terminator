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
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface InterstitialRepository {

    fun observe(): Observable<InterstitialEvent>

    fun getLastEvent(): InterstitialEvent

    @MainThread
    fun show(): Completable

    companion object {
        internal fun Scope.factoryInterstitialRepository(activity: AppCompatActivity): InterstitialRepository = InterstitialRepositoryImpl(
            activity = activity,
            adUnitId = get(named(AdsConstants.INTERSTITIAL_ADS_UNIT_ID))
        )

        internal fun factoryStubInterstitialRepository() = object : InterstitialRepository {
            override fun observe() = Observable.never<InterstitialEvent>()
            override fun getLastEvent() = InterstitialEvent.NotLoadedYet
            override fun show() = Completable.complete()
        }
    }
}

internal class InterstitialRepositoryImpl(
        private val activity: AppCompatActivity,
        private val adUnitId: String
) : InterstitialRepository,
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

    override fun getLastEvent() = requireNotNull(subject.value)

    override fun show() = Completable.fromAction {
        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        }
    }

    private fun loadNewAd() {
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

}
