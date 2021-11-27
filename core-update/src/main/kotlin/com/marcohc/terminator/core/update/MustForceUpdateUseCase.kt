package com.marcohc.terminator.core.update

import com.marcohc.terminator.core.remoteconfig.RemoteConfigRepository
import io.reactivex.Single
import timber.log.Timber

class MustForceUpdateUseCase(
    private val appVersionCode: Int,
    private val remoteConfigRepository: RemoteConfigRepository
) {

    fun execute() = Single.fromCallable {
        val forceUpdateVersion = remoteConfigRepository.getLong(FORCE_UPDATE_VERSION)
        val mustForceUpdate = forceUpdateVersion > appVersionCode
        Timber.v("$forceUpdateVersion (forceUpdateVersion) > $appVersionCode (appVersionCode) = $mustForceUpdate")
        mustForceUpdate
    }

    companion object {
        const val FORCE_UPDATE_VERSION = "force_update_version"
    }
}
