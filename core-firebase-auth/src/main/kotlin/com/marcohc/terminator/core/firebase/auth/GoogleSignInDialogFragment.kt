package com.marcohc.terminator.core.firebase.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.marcohc.terminator.core.mvi.ui.MviConfig
import com.marcohc.terminator.core.mvi.ui.MviConfigType
import com.marcohc.terminator.core.mvi.ui.MviDialogFragment
import com.marcohc.terminator.core.firebase.auth.GoogleSignInIntention.ActivityResult
import com.marcohc.terminator.core.firebase.auth.GoogleSignInIntention.Initial

class GoogleSignInDialogFragment : MviDialogFragment<GoogleSignInIntention, GoogleSignInState>() {

    override val mviConfig = MviConfig(
        scopeId = GoogleSignInModule.scopeId,
        layoutId = R.layout.google_sign_in_dialog_fragment,
        mviConfigType = MviConfigType.SCOPE_AND_NAVIGATION
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        sendIntention(ActivityResult(requestCode, intent))
    }

    override fun afterComponentCreated(savedInstanceState: Bundle?) {
        sendIntention(Initial)
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
            setLayout(MATCH_PARENT, MATCH_PARENT)
            addFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    companion object {
        fun newInstance() = GoogleSignInDialogFragment()
    }

}
