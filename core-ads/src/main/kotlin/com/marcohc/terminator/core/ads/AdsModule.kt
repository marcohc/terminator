package com.marcohc.terminator.core.ads

import com.marcohc.terminator.core.koin.FeatureModule
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object AdsModule : FeatureModule {

    override val scopeId: String = "AdsModule"

    override val module: Module = module {
        scope(named(scopeId)) {  }
    }

}
