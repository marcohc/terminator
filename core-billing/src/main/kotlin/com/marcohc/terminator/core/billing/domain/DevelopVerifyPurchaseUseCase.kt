package com.marcohc.terminator.core.billing.domain

import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.VerifyPurchaseUseCase
import io.reactivex.Completable

internal class DevelopVerifyPurchaseUseCase : VerifyPurchaseUseCase {
    override fun execute(purchase: PurchaseEntity) = Completable.complete()
}
