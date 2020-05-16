package com.marcohc.terminator.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.marcohc.terminator.core.koin.CoreModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

object RemoteConfigModule : CoreModule {

    override val module = module {
        single {
            RemoteConfigRepository(firebaseRemoteConfig = FirebaseRemoteConfig
                .getInstance()
                .apply {
                    setConfigSettingsAsync(
                        FirebaseRemoteConfigSettings.Builder()
                            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                            .build()
                    )
                    setDefaultsAsync(get<Int>(named(REMOTE_CONFIG_DEFAULT)))
                }
            )
        }
    }

    const val REMOTE_CONFIG_DEFAULT = "REMOTE_CONFIG_DEFAULT"

}
