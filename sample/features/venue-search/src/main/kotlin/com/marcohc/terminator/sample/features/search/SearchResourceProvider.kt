package com.marcohc.terminator.sample.features.search

import android.content.Context

internal class SearchResourceProvider(
    private val context: Context
) {

    fun getNoItemsString(city: String): String = context.getString(R.string.search_no_items, city)
    fun getLocationUnknownText(): String = context.getString(R.string.search_detail_location_unknown)
    fun getGeneralErrorText(): String = context.getString(R.string.widget_general_error)

}
