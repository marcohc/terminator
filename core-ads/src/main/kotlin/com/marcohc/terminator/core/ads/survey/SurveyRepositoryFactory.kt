package com.marcohc.terminator.core.ads.survey

import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.AdsConstants
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

object SurveyRepositoryFactory : KoinComponent {

    fun newInstance(activity: AppCompatActivity): SurveyRepository = SurveyRepositoryImpl(
        activity = activity,
        debug = get(named(AdsConstants.SURVEY_DEBUG)),
        apiKey = get(named(AdsConstants.SURVEY_API_KEY))
    )

}
