package com.marcohc.terminator.core.ads.survey

interface SurveyAnalytics {
    fun logClick()
    fun logNotAvailable()
    fun logAvailable(price: Double)
    fun logOpened()
    fun logUserRejected()
    fun logNotEligible()
    fun logCompleted(price: Double)
    fun logClosed()
}
