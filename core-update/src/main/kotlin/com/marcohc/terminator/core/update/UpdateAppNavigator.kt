package com.marcohc.terminator.core.update

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

class UpdateAppNavigator {

    fun showUpdate(activity: Activity, callback: (Boolean) -> Unit) {
        AppUpdateManagerFactory
            .create(activity)
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isImmediateUpdateAllow = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                Timber.d("UpdateAppNavigator: isImmediateUpdateAllow ($isImmediateUpdateAllow) / isUpdateAvailable ($isUpdateAvailable)")
                if (isImmediateUpdateAllow && isUpdateAvailable) {
                    AppUpdateManagerFactory
                        .create(activity)
                        .startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            REQUEST_CODE_UPDATE_APP
                        )
                    callback.invoke(true)
                } else {
                    callback.invoke(false)
                }
            }
            .addOnFailureListener {
                Timber.w(it, "UpdateAppNavigator: ${it.message}")
                callback.invoke(false)
            }
    }

    companion object {
        const val REQUEST_CODE_UPDATE_APP = 1212
    }
}
