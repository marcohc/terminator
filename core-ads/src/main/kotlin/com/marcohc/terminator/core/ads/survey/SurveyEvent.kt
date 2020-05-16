package com.marcohc.terminator.core.ads.survey

sealed class SurveyEvent {
    object NotLoadedYet : SurveyEvent()
    object NotAvailable : SurveyEvent()
    data class Received(val surveyPrice: Double) : SurveyEvent()
    object Opened : SurveyEvent()
    object UserRejected : SurveyEvent()
    object NotEligible : SurveyEvent()
    data class Rewarded(val surveyPrice: Double) : SurveyEvent()
    object Closed : SurveyEvent()
}
