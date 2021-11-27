package com.marcohc.terminator.core.billing.domain

import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import io.reactivex.Completable

/**
 * Implement your own class completing or throwing error if the purchase is not valid
 */
interface VerifyPurchaseUseCase {

    fun execute(purchase: PurchaseEntity): Completable
}
