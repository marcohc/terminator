package com.marcohc.terminator.core.serializer

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun <T> Gson.toMap(dataClass: T): Map<String, Any> {
    return convert(dataClass)
}

inline fun <reified T> Gson.toDataClass(map: Map<String, Any>): T {
    return convert(map)
}

inline fun <I, reified O> Gson.convert(anything: I): O {
    return fromJson(toJson(anything), object : TypeToken<O>() {}.type)
}

inline fun <reified T> Gson.parseList(jsonObject: Any, clazz: Class<Array<T>>): List<T> {
    return fromJson(toJson(jsonObject), clazz).toList()
}
