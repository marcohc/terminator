package com.marcohc.terminator.sample.features.search

import android.app.Activity

interface SearchNavigator {

    fun goToVenue(activity: Activity, requestCode: Int, id: String)

}
