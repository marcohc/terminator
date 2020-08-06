package com.marcohc.terminator.core.survey

import com.marcohc.terminator.core.koin.FeatureModule
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object SurveyModule : FeatureModule {

    override val scopeId: String = "SurveyModule"

    override val module: Module = module {
        scope(named(scopeId)) {  }
    }

}
