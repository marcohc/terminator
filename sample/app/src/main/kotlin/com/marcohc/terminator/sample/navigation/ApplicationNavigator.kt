package com.marcohc.terminator.sample.navigation

import android.app.Activity
import com.marcohc.terminator.sample.features.detail.DetailActivity
import com.marcohc.terminator.sample.features.search.SearchNavigator

class ApplicationNavigator : SearchNavigator {

    override fun goToVenue(activity: Activity, requestCode: Int, id: String) {
        activity.startActivityForResult(DetailActivity.newInstance(activity, id), requestCode)
    }
}
