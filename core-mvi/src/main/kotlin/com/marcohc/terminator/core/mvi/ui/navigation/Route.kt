package com.marcohc.terminator.core.mvi.ui.navigation

import kotlin.reflect.KClass

private fun KClass<out Route>.tag(): String = this.java.name

abstract class Route {
    val tag: String = this::class.tag()
}
