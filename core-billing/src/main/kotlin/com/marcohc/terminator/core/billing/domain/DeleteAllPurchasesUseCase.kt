package com.marcohc.terminator.core.billing.domain

import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepository

internal class DeleteAllPurchasesUseCase(private val repository: PurchaseRepository) {

    fun execute() = repository.deleteAll()
}
