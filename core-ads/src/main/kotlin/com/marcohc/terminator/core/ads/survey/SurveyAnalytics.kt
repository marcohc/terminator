package com.marcohc.terminator.core.ads.survey

import android.os.Bundle
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

interface SurveyAnalytics {
    fun logEvent(event: SurveyEvent): Completable

    fun logClick(): Completable
}

internal class SurveyAnalyticsImpl(
        private val analytics: Analytics,
        private val scopeId: String
) : SurveyAnalytics {

    override fun logEvent(event: SurveyEvent) = Completable.fromAction {
        when (event) {
            is SurveyEvent.NotAvailable -> logEvents("not_available")
            is SurveyEvent.Received -> logEvents("available")
            is SurveyEvent.Opened -> {
                analytics.logCheckoutStart(
                    value = event.surveyPrice,
                    currency = "USD"
                )
                logEvents("opened")
            }
            is SurveyEvent.UserRejected -> logEvents("user_rejected")
            is SurveyEvent.NotEligible -> logEvents("not_eligible")
            is SurveyEvent.Rewarded -> {
                analytics.logCheckoutEnd(
                    value = event.surveyPrice,
                    currency = "USD"
                )
                logEvents("rewarded")
            }
            is SurveyEvent.Closed -> logEvents("closed")
        }
    }

    override fun logClick() = Completable.fromAction {
        analytics.logClick(scopeId, "${BASE_EVENT}_click")
        logEvents("click")
    }

    private fun logEvents(parameter: String) {
        analytics.logEvent(BASE_EVENT, Bundle().apply { putString("${BASE_EVENT}_action", "${scopeId}_${parameter}") })
        analytics.logEvent("${scopeId}_${BASE_EVENT}_${parameter}")
    }

    private companion object {
        const val BASE_EVENT = "survey"
    }
}
