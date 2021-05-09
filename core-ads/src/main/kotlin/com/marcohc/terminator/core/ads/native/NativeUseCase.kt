package com.marcohc.terminator.core.ads.native

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
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

    fun getReadyToShowAd(): Single<Optional<UnifiedNativeAd>>

    @MainThread
    fun markLastAsSeen(): Completable

    companion object {

        fun Scope.getOrCreateScopedNativeUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ) = getOrCreateFromParentScope(AdsModule.scopeId) { factoryNativeUseCase(analyticsScopeId, activity) }

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
            override fun getReadyToShowAd() = Single.never<Optional<UnifiedNativeAd>>()
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
    LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val subject = BehaviorSubject.createDefault<NativeEvent>(NativeEvent.NotLoadedYet)
    private var adLoader: AdLoader? = null
    private var nativeAdsList = mutableListOf<NativeAd>()

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        MobileAds.initialize(activity) {}

        // Track events
        compositeDisposable.add(
            subject
                .flatMapCompletable(analytics::logEvent)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )

        adLoader = AdLoader.Builder(activity, adUnitId)
            .forUnifiedNativeAd { unifiedNativeAd: UnifiedNativeAd ->
                val nativeAd = NativeAd(unifiedNativeAd)
                Timber.v("$nativeAd")
                nativeAdsList.add(nativeAd)
            }
            .withAdListener(
                object : AdListener() {
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
                }
            )
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        nativeAdsList.forEach { it.unifiedNativeAd.destroy() }
        nativeAdsList.clear()
        compositeDisposable.clear()
    }

    override fun getReadyToShowAd() = Single.fromCallable {
        val firstNotSeen = getFirstNotSeen()
        if (firstNotSeen == null) {
            None
        } else {
            Some(firstNotSeen.unifiedNativeAd)
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
                AdRequest.Builder()
                    // Use options
                    .build()
                , numberOfAds
            )
        }
    }

    private data class NativeAd(
            val unifiedNativeAd: UnifiedNativeAd,
            var seen: Boolean = false
    ) {
        override fun toString(): String {
            return "NativeAd(unifiedNativeAd=${unifiedNativeAd.headline}, seen=$seen, hasVideoContent: ${unifiedNativeAd.mediaContent.videoController.hasVideoContent()})"
        }
    }

}
