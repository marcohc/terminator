package com.marcohc.terminator.core.ads.survey

import android.os.Bundle
import com.marcohc.terminator.core.analytics.Analytics
import io.reactivex.Completable

internal class SurveyAnalyticsImpl(
        private val analytics: Analytics,
        private val scopeId: String
) : SurveyAnalytics {

    override fun logClick() = Completable.fromAction {
        analytics.logClick(scopeId, "survey_click")
        logEvent("click")
    }

    override fun logEvent(event: SurveyEvent) = Completable.fromAction {
        when (event) {
            is SurveyEvent.NotAvailable -> logEvent("not_available")
            is SurveyEvent.Received -> {
                analytics.logCheckoutStart(
                    value = event.surveyPrice,
                    currency = "USD"
                )
                logEvent("available")
            }
            is SurveyEvent.Opened -> logEvent("opened")
            is SurveyEvent.UserRejected -> logEvent("user_rejected")
            is SurveyEvent.NotEligible -> logEvent("not_eligible")
            is SurveyEvent.Rewarded -> {
                analytics.logCheckoutEnd(
                    value = event.surveyPrice,
                    currency = "USD"
                )
                logEvent("completed")
            }
            is SurveyEvent.Closed -> logEvent("closed")
        }
    }

    private fun logEvent(parameter: String) {
        val parameterKey = parameter + "_" + scopeId
        analytics.logCustomEvent("survey", Bundle().apply { putString("survey_action", parameterKey) })
    }
}
