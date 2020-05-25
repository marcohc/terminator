package com.marcohc.terminator.core.ads.survey

import com.marcohc.terminator.core.ads.survey.SurveyRepository.Companion.factoryStubSurveyRepository
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.scope.Scope

class SurveyUseCase private constructor(
        private val repository: SurveyRepository,
        private val analytics: SurveyAnalytics
) {

    fun observeAndTrack(): Observable<SurveyEvent> = repository.observe()
        .flatMap { event -> analytics.logEvent(event).toSingleDefault(event).toObservable() }

    fun logShowSurveyClick() = Completable.fromAction { analytics.logClick() }

    fun show() = repository.openSurvey()

    companion object {
        fun Scope.factorySurveyUseCase(scopeId: String) = SurveyUseCase(
            repository = getScope(scopeId).get(),
            analytics = SurveyAnalyticsImpl(
                analytics = get(),
                scopeId = scopeId
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
