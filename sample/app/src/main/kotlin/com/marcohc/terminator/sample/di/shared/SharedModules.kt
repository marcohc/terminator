package com.marcohc.terminator.sample.di.shared

import com.marcohc.terminator.sample.data.di.DataModule

object SharedModules {

    val modules = listOf(
        DataModule.module,
        NavigationModule.module
    )

}

