package com.marcohc.terminator.core.firebase.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.marcohc.terminator.core.firebase.auth.GoogleSignInIntention.ActivityResult
import com.marcohc.terminator.core.firebase.auth.GoogleSignInIntention.Initial
import com.marcohc.terminator.core.mvi.ui.MviConfig
import com.marcohc.terminator.core.mvi.ui.MviConfigType
import com.marcohc.terminator.core.mvi.ui.MviDialogFragment

class GoogleSignInDialogFragment : MviDialogFragment<GoogleSignInIntention, GoogleSignInState>() {

    override val mviConfig = MviConfig(
        scopeId = AuthModule.scopeId,
        layoutId = R.layout.google_sign_in_dialog_fragment,
        mviConfigType = MviConfigType.SCOPE_AND_NAVIGATION
    )

    private val intent: Intent? by lazy { arguments?.getParcelable(INTENT_EXTRA) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        sendIntention(ActivityResult(requestCode, intent))
    }

    override fun afterComponentCreated(savedInstanceState: Bundle?) {
        sendIntention(Initial(intent))
    }

    override fun render(state: GoogleSignInState) {
        // No-op
    }

    override fun onStart() {
        super.onStart()
        adjustFullScreenDialog()
    }

    private fun adjustFullScreenDialog() {
        dialog?.window?.run {
            setLayout(MATCH_PARENT, WRAP_CONTENT)
            addFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    companion object {

        private const val INTENT_EXTRA = "INTENT_EXTRA"

        fun newInstance(intent: Intent? = null) = GoogleSignInDialogFragment()
            .apply {
                intent?.run { arguments = Bundle().apply { putParcelable(INTENT_EXTRA, intent) } }
            }
    }
}
