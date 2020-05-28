package com.marcohc.terminator.core.ads.interstitial

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.AdsModule
import com.marcohc.terminator.core.ads.interstitial.InterstitialRepository.Companion.factoryInterstitialRepository
import com.marcohc.terminator.core.ads.interstitial.InterstitialRepository.Companion.factoryStubInterstitialRepository
import com.marcohc.terminator.core.mvi.ext.fetchOrCreateFromParentScope
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.scope.Scope

class InterstitialUseCase private constructor(
        private val repository: InterstitialRepository,
        private val analytics: InterstitialAnalytics
) {

    fun observeAndTrack(): Observable<InterstitialEvent> = repository.observe()
        .flatMap { event -> analytics.logEvent(event).toSingleDefault(event).toObservable() }

    fun getLastEvent() = repository.getLastEvent()

    @MainThread
    fun show() = repository.show()

    companion object {
        fun Scope.factoryInterstitialUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ) = InterstitialUseCase(
            repository = fetchOrCreateFromParentScope(AdsModule.scopeId) {
                factoryInterstitialRepository(activity)
            },
            analytics = InterstitialAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            )
        )

        fun factoryStubInterstitialUseCase() = InterstitialUseCase(
            repository = factoryStubInterstitialRepository(),
            analytics = object : InterstitialAnalytics {
                override fun logEvent(event: InterstitialEvent) = Completable.complete()
            }
        )
    }
}
