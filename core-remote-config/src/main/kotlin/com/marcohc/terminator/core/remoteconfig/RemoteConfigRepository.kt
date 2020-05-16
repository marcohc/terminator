package com.marcohc.terminator.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.core.KoinComponent

class RemoteConfigRepository(private val firebaseRemoteConfig: FirebaseRemoteConfig) : KoinComponent {

    fun fetchAndActivateNextAppStart() {
        firebaseRemoteConfig.fetch()
        firebaseRemoteConfig.activate()
    }

    fun getBoolean(key: String) = firebaseRemoteConfig.getBoolean(key)

    fun getString(key: String) = firebaseRemoteConfig.getString(key)

    fun getDouble(key: String) = firebaseRemoteConfig.getDouble(key)

    fun getLong(key: String) = firebaseRemoteConfig.getLong(key)

    fun getValue(key: String) = firebaseRemoteConfig.getValue(key)

}
