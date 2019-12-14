package com.marcohc.terminator.core.mvi.domain

interface KotlinLogger {

    fun v(tag: String, message: String)

    fun e(throwable: Throwable?)

}
