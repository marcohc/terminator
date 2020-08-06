package com.marcohc.terminator.core.survey

sealed class SurveyEvent {
    object NotLoadedYet : SurveyEvent()
    object NotAvailable : SurveyEvent()
    data class Received(val surveyPrice: Double) : SurveyEvent()
    data class Opened(val surveyPrice: Double) : SurveyEvent()
    object UserRejected : SurveyEvent()
    object NotEligible : SurveyEvent()
    data class Rewarded(val surveyPrice: Double) : SurveyEvent()
    object Closed : SurveyEvent()
}
