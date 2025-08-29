package com.gojuno.koptional

abstract class Optional<out T : Any> internal constructor() {

    abstract fun toNullable(): T?

    abstract operator fun component1(): T?

    companion object {
        @JvmStatic
        fun <T : Any> toOptional(value: T?): Optional<T> =
            if (value == null) None else Some(value)
    }
}

data class Some<out T : Any> constructor(val value: T) : Optional<T>() {
    override fun toNullable(): T = value
    override fun toString() = "Some($value)"
}

object None : Optional<Nothing>() {
    override fun component1(): Nothing? = null
    override fun toNullable(): Nothing? = null
    override fun toString() = "None"
}

fun <T : Any> T?.toOptional(): Optional<T> = if (this == null) None else Some(this)
