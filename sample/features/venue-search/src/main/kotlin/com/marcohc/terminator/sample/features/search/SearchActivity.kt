package com.marcohc.terminator.sample.features.search

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.marcohc.terminator.core.mvi.ui.MviActivity
import com.marcohc.terminator.core.mvi.ui.MviConfig
import com.marcohc.terminator.core.mvi.ui.MviConfigType
import com.marcohc.terminator.core.utils.setGone
import com.marcohc.terminator.core.utils.setVisible
import com.marcohc.terminator.sample.features.search.adapter.VenueAdapter
import com.marcohc.terminator.sample.features.search.adapter.VenueItem
import kotlinx.android.synthetic.main.search_activity.search_edit_text
import kotlinx.android.synthetic.main.search_activity.search_progress_bar
import kotlinx.android.synthetic.main.search_activity.search_recycler_view
import kotlinx.android.synthetic.main.search_activity.search_status_text

class SearchActivity : MviActivity<SearchIntention, SearchState>() {

    override val mviConfig = MviConfig(
        scopeId = SearchModule.scopeId,
        layoutId = R.layout.search_activity,
        mviConfigType = MviConfigType.SCOPE_AND_NAVIGATION
    )

    private lateinit var recyclerAdapter: VenueAdapter

    override fun afterComponentCreated(savedInstanceState: Bundle?) {
        search_progress_bar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)

        recyclerAdapter = VenueAdapter()
        val layoutManager = LinearLayoutManager(this)
        search_recycler_view.apply {
            adapter = recyclerAdapter
            this.layoutManager = layoutManager
            addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        }

        recyclerAdapter.setOnItemClickListener { _, _, item ->
            when (item) {
                is VenueItem.Venue -> sendIntention(SearchIntention.ItemClick(item))
            }
        }

        search_edit_text.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(this)
                sendIntention(SearchIntention.Search(search_edit_text.text.toString()))
                true
            } else {
                false
            }
        }

        sendIntention(SearchIntention.Initial)
    }

    override fun render(state: SearchState) {
        with(state) {
            if (loading) {
                search_progress_bar.setVisible()
                search_status_text.setGone()
                recyclerAdapter.setData(emptyList())
            } else {
                search_progress_bar.setGone()
                search_status_text.setVisible()
            }

            search_status_text.text = status
            recyclerAdapter.setData(items)
            connected.consume()?.let { isConnected ->
                val message = if (isConnected) {
                    R.string.search_connected
                } else {
                    R.string.search_not_connected
                }
                Toast.makeText(this@SearchActivity, message, Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}
