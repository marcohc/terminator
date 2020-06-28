@file:Suppress("unused")

package com.marcohc.terminator.core.utils

fun <E> List<E>.exist(function: (E) -> Boolean) = find { function.invoke(it) } != null
