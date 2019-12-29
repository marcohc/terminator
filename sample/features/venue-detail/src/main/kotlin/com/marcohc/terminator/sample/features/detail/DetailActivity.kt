package com.marcohc.terminator.sample.features.detail

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.marcohc.terminator.core.mvi.ui.MviActivity
import com.marcohc.terminator.core.mvi.ui.MviConfig
import com.marcohc.terminator.core.mvi.ui.MviConfigType
import com.marcohc.terminator.core.utils.setGone
import com.marcohc.terminator.core.utils.setVisible
import kotlinx.android.synthetic.main.detail_activity.detail_contact_text
import kotlinx.android.synthetic.main.detail_activity.detail_description_text
import kotlinx.android.synthetic.main.detail_activity.detail_location_text
import kotlinx.android.synthetic.main.detail_activity.detail_photo_image
import kotlinx.android.synthetic.main.detail_activity.detail_progress_bar
import kotlinx.android.synthetic.main.detail_activity.detail_rating_text
import kotlinx.android.synthetic.main.detail_activity.detail_status_text
import kotlinx.android.synthetic.main.detail_activity.detail_title_text
import kotlinx.android.synthetic.main.detail_activity.details_toolbar
import kotlin.math.roundToInt

class DetailActivity : MviActivity<DetailIntention, DetailState>() {

    override val mviConfig = MviConfig(
        scopeId = DetailModule.scopeId,
        layoutId = R.layout.detail_activity,
        mviConfigType = MviConfigType.NO_SCOPE
    )

    override fun afterComponentCreated(savedInstanceState: Bundle?) {
        details_toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        details_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        detail_progress_bar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)

        sendIntention(DetailIntention.Initial(checkNotNull(intent.getStringExtra(EXTRA_VENUE_ID)) { "You must pass a venueId to this screen" }))
    }

    override fun render(state: DetailState) {
        when (state) {
            DetailState.Loading -> {
                detail_progress_bar.setVisible()
            }
            is DetailState.Data -> {
                detail_progress_bar.setGone()
                detail_status_text.setGone()

                state.venue.run {
                    detail_title_text.text = name
                    // This view logic could be moved and tested into the Interactor to keep View as stupid as possible
                    detail_description_text.text = if (description.isEmpty()) getString(R.string.detail_description_unknown) else description
                    detail_location_text.text = if (location.isEmpty()) getString(R.string.detail_location_unknown) else location
                    detail_rating_text.text = if (rating == 0.0) getString(R.string.detail_rating_unknown) else getString(R.string.detail_rating, rating.roundToInt())
                    detail_contact_text.text = if (phone.isEmpty()) getString(R.string.detail_phone_unknown) else phone

                    val requestOptions = RequestOptions()
                        .error(android.R.drawable.ic_menu_report_image)

                    Glide.with(this@DetailActivity)
                        .load(pictureUrl)
                        .apply(requestOptions)
                        .into(detail_photo_image)
                }
            }
            is DetailState.Error -> {
                detail_status_text.setVisible()
                detail_status_text.text = state.errorText
            }
        }
    }

    companion object {
        private const val EXTRA_VENUE_ID = "extra_venue_id"

        fun newInstance(context: Context, id: String): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_VENUE_ID, id)
            intent.putExtras(bundle)
            return intent
        }
    }

}
