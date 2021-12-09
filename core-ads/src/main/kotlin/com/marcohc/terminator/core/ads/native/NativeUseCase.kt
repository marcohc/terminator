package com.marcohc.terminator.core.ads.native

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.marcohc.terminator.core.ads.AdsConstants.NATIVE_ADS_UNIT_ID
import com.marcohc.terminator.core.ads.AdsModule
import com.marcohc.terminator.core.mvi.ext.getOrCreateFromParentScope
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber

interface NativeUseCase {

    fun getReadyToShowAd(): Single<Optional<NativeAd>>

    @MainThread
    fun markLastAsSeen(): Completable

    companion object {

        fun Scope.getOrCreateScopedNativeUseCase(
            analyticsScopeId: String,
            activity: AppCompatActivity
        ) = getOrCreateFromParentScope(AdsModule.scopeId) {
            factoryNativeUseCase(
                analyticsScopeId,
                activity
            )
        }

        fun Scope.factoryNativeUseCase(
            analyticsScopeId: String,
            activity: AppCompatActivity
        ): NativeUseCase = NativeUseCaseImpl(
            activity = activity,
            analytics = NativeAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            ),
            adUnitId = get(named(NATIVE_ADS_UNIT_ID)),
            adChoicesPlacement = get(named(NATIVE_ADS_ADCHOICES_POSITION)),
            numberOfAds = get(named(NATIVE_ADS_NUMBER_OF_ADS))
        )

        fun factoryStubNativeUseCase() = object : NativeUseCase {
            override fun getReadyToShowAd() = Single.never<Optional<NativeAd>>()
            override fun markLastAsSeen() = Completable.complete()
        }

        const val NATIVE_ADS_ADCHOICES_POSITION = "ADCHOICES_POSITION"
        const val NATIVE_ADS_NUMBER_OF_ADS = "NATIVE_ADS_NUMBER_OF_ADS"
    }

}

internal class NativeUseCaseImpl(
    private val activity: AppCompatActivity,
    private val analytics: NativeAnalytics,
    private val adUnitId: String,
    private val adChoicesPlacement: Int,
    private val numberOfAds: Int
) : NativeUseCase,
    DefaultLifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val subject = BehaviorSubject.createDefault<NativeEvent>(NativeEvent.NotLoadedYet)
    private var adLoader: AdLoader? = null
    private var nativeAdsList = mutableListOf<MyNativeAd>()

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {

        MobileAds.initialize(activity) {}

        // Track events
        compositeDisposable.add(
            subject
                .flatMapCompletable(analytics::trackEvent)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )

        adLoader = AdLoader.Builder(activity, adUnitId)
            .forNativeAd { nativeAd: NativeAd ->
                val myNativeAd = MyNativeAd(nativeAd)
                Timber.v("$myNativeAd")
                nativeAdsList.add(myNativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    Timber.v("NativeEvent.onAdLoaded")
                    subject.onNext(NativeEvent.Loaded)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.v("NativeEvent.FailedToLoad: $loadAdError")
                    subject.onNext(NativeEvent.FailedToLoad)
                }

                override fun onAdOpened() {
                    Timber.v("NativeEvent.Opened")
                    subject.onNext(NativeEvent.Opened)
                }

                override fun onAdImpression() {
                    Timber.v("NativeEvent.onAdImpression")
                    subject.onNext(NativeEvent.Impression)
                }

                override fun onAdClosed() {
                    Timber.v("NativeEvent.Closed")
                    subject.onNext(NativeEvent.Closed)
                }

                override fun onAdClicked() {
                    Timber.v("NativeEvent.Click")
                    subject.onNext(NativeEvent.Click)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(adChoicesPlacement)
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build()
            )
            .build()

        loadAd()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        nativeAdsList.forEach { it.nativeAd.destroy() }
        nativeAdsList.clear()
        compositeDisposable.clear()
    }

    override fun getReadyToShowAd() = Single.fromCallable {
        val firstNotSeen = getFirstNotSeen()
        if (firstNotSeen == null) {
            None
        } else {
            Some(firstNotSeen.nativeAd)
        }
    }

    @MainThread
    override fun markLastAsSeen() = Completable.fromAction {
        val nativeAd = getFirstNotSeen()
        nativeAd?.seen = true
        loadAd()
    }

    private fun getFirstNotSeen() = nativeAdsList.firstOrNull { !it.seen }

    private fun loadAd() {
        val notSeenItems = nativeAdsList.count { !it.seen }
        if (notSeenItems < numberOfAds) {
            adLoader?.loadAds(
                AdRequest.Builder().build(),
                numberOfAds
            )
        }
    }

    private data class MyNativeAd(
        val nativeAd: NativeAd,
        var seen: Boolean = false
    ) {
        override fun toString(): String {
            return "NativeAd(unifiedNativeAd=${nativeAd.headline}, seen=$seen, hasVideoContent: ${nativeAd.mediaContent?.videoController?.hasVideoContent()})"
        }
    }
}
