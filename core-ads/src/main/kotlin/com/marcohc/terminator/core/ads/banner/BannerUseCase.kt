package com.marcohc.terminator.core.ads.banner

import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.banner.BannerRepository.Companion.factoryBannerRepository
import com.marcohc.terminator.core.ads.banner.BannerRepository.Companion.factoryStubBannerRepository
import com.marcohc.terminator.core.mvi.ext.fetchOrCreateFromParentScope
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.scope.Scope

class BannerUseCase private constructor(
        private val repository: BannerRepository,
        private val analytics: BannerAnalytics
) {

    fun observeAndTrack(): Observable<BannerEvent> = repository.observe()
        .flatMap { event -> analytics.logEvent(event).toSingleDefault(event).toObservable() }

    companion object {
        fun Scope.factoryBannerUseCase(
                libraryScopeId: String,
                analyticsScopeId: String,
                activity: AppCompatActivity
        ) = BannerUseCase(
            repository = fetchOrCreateFromParentScope(libraryScopeId) {
                factoryBannerRepository(activity)
            },
            analytics = BannerAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            )
        )

        fun factoryStubBannerUseCase() = BannerUseCase(
            repository = factoryStubBannerRepository(),
            analytics = object : BannerAnalytics {
                override fun logEvent(event: BannerEvent) = Completable.complete()
            }
        )
    }
}
