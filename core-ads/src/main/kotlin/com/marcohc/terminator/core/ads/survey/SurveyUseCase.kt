package com.marcohc.terminator.core.ads.survey

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.AdsModule
import com.marcohc.terminator.core.ads.survey.SurveyRepository.Companion.factoryStubSurveyRepository
import com.marcohc.terminator.core.ads.survey.SurveyRepository.Companion.factorySurveyRepository
import com.marcohc.terminator.core.mvi.ext.fetchOrCreateFromParentScope
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.scope.Scope

class SurveyUseCase private constructor(
        private val repository: SurveyRepository,
        private val analytics: SurveyAnalytics
) {

    fun observe(): Observable<SurveyEvent> = repository.observe()

    fun observeAndTrack(): Observable<SurveyEvent> = repository.observe()
        .flatMap { event -> analytics.logEvent(event).toSingleDefault(event).toObservable() }

    fun logShowSurveyClick() = Completable.fromAction { analytics.logClick() }

    fun getLastEvent() = repository.getLastEvent()

    @MainThread
    fun show() = repository.show()

    companion object {
        fun Scope.factorySurveyUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ) = SurveyUseCase(
            repository = fetchOrCreateFromParentScope(AdsModule.scopeId) {
                factorySurveyRepository(activity)
            },
            analytics = SurveyAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            )
        )

        fun factoryStubSurveyUseCase() = SurveyUseCase(
            repository = factoryStubSurveyRepository(),
            analytics = object : SurveyAnalytics {
                override fun logEvent(event: SurveyEvent) = Completable.complete()
                override fun logClick() = Completable.complete()
            }
        )
    }
}
