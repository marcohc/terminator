package com.marcohc.terminator.core.firebase.auth

import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.marcohc.terminator.core.mvi.ui.navigation.FragmentNavigationExecutor

internal class GoogleSignInRouter(
    private val executor: FragmentNavigationExecutor,
    private val options: GoogleSignInOptions
) {

    fun showSignInDialog() = executor.executeCompletable { fragment ->
        fragment.activity?.let { activity ->
            fragment.startActivityForResult(
                GoogleSignIn.getClient(activity, options).signInIntent,
                REQUEST_CODE_SIGN_IN
            )
        }
    }

    fun dismiss() = executor.executeCompletable { fragment ->
        (fragment as DialogFragment).dismiss()
    }

    internal companion object {
        const val REQUEST_CODE_SIGN_IN = 0x1234
    }
}
