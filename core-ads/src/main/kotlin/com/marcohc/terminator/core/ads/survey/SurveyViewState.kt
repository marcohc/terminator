package com.marcohc.terminator.core.ads.survey

sealed class SurveyViewState {
    object Loading : SurveyViewState()
    object SurveyNotAvailable : SurveyViewState()
    data class SurveyAvailable(val earnTimeText: String) : SurveyViewState()
    data class Completed(val rewardText: String) : SurveyViewState()
}
