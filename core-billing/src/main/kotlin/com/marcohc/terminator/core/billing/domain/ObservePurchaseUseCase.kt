package com.marcohc.terminator.core.billing.domain

import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepository
import com.marcohc.terminator.core.utils.observableJust
import com.marcohc.terminator.core.utils.toObservableDefault
import io.reactivex.Observable

class ObservePurchaseUseCase(
        private val verifyPurchaseUseCase: VerifyPurchaseUseCase,
        private val repository: PurchaseRepository
) {

    fun execute(): Observable<Optional<PurchaseEntity>> = repository.observe()
        .flatMap { purchaseOptional ->
            when (purchaseOptional) {
                is Some -> verifyPurchaseUseCase.execute(purchaseOptional.value)
                    .toObservableDefault(Some(purchaseOptional.value))
                else -> None.observableJust()
            }
        }
}
