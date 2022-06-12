package com.marcohc.terminator.core.billing.data.api

internal sealed class GoogleBillingResponse<out T : Any> {
    data class Success<out T : Any>(val result: T) : GoogleBillingResponse<T>()
    data class Failure(val result: Int) : GoogleBillingResponse<Nothing>()
}
