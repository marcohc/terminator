package com.marcohc.terminator.core.ads.survey

import io.reactivex.Completable

interface SurveyAnalytics {
    fun logEvent(event: SurveyEvent): Completable

    fun logClick(): Completable
}
