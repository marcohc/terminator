package com.marcohc.terminator.core.firebase.auth

import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutor

internal class GoogleSignInRouter(
        private val navigationExecutor: FragmentNavigationExecutor,
        private val builder: GoogleSignInOptions.Builder
) {

    fun showSignInDialog() = navigationExecutor.executeCompletable { fragment ->
        fragment.activity?.let { activity ->
            fragment.startActivityForResult(GoogleSignIn.getClient(activity, builder.build()).signInIntent, REQUEST_CODE_SIGN_IN)
        }
    }

    fun dismiss() = navigationExecutor.executeCompletable { fragment ->
        (fragment as DialogFragment).dismiss()
    }

    internal companion object {
        const val REQUEST_CODE_SIGN_IN = 0x1234
    }

}
