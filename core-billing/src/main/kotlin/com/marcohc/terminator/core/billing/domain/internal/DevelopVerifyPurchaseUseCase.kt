package com.marcohc.terminator.core.billing.domain.internal

import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.domain.VerifyPurchaseUseCase
import io.reactivex.Completable

internal class DevelopVerifyPurchaseUseCase : VerifyPurchaseUseCase {

    override fun execute(purchase: PurchaseEntity) = Completable.complete()
}
