package com.marcohc.terminator.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber

class RemoteConfigRepository(private val firebaseRemoteConfig: FirebaseRemoteConfig) {

    fun fetchAndActivateNextAppStart() {
        firebaseRemoteConfig.fetch()
        firebaseRemoteConfig.activate()
    }

    fun getBoolean(key: String) = firebaseRemoteConfig.getBoolean(key).apply { Timber.v("key: $key, value: $this") }

    fun getString(key: String) = firebaseRemoteConfig.getString(key).apply { Timber.v("key: $key, value: $this") }

    fun getDouble(key: String) = firebaseRemoteConfig.getDouble(key).apply { Timber.v("key: $key, value: $this") }

    fun getLong(key: String) = firebaseRemoteConfig.getLong(key).apply { Timber.v("key: $key, value: $this") }

    fun getValue(key: String) = firebaseRemoteConfig.getValue(key).apply { Timber.v("key: $key, value: $this") }

}
