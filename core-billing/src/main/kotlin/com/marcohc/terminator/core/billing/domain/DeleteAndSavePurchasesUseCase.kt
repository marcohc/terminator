package com.marcohc.terminator.core.billing.domain

import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepository

internal class DeleteAndSavePurchasesUseCase(private val repository: PurchaseRepository) {

    fun execute(purchase: PurchaseEntity) = repository.deleteAllAndSave(purchase)
}
