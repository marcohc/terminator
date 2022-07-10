package com.marcohc.terminator.core.billing

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.data.models.Subscription
import com.marcohc.terminator.core.billing.data.repositories.BillingRepository
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepository
import com.marcohc.terminator.core.utils.observableJust
import com.marcohc.terminator.core.utils.toObservableDefault
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class BillingUseCase(
    private val billingRepository: BillingRepository,
    private val purchaseRepository: PurchaseRepository,
    private val verifyPurchaseUseCase: VerifyPurchaseUseCase,
) : DefaultLifecycleObserver {

    //region Lifecycle events to connect and disconnect the API
    override fun onCreate(owner: LifecycleOwner) {
        billingRepository.connect()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        billingRepository.disconnect()
    }
    //endregion

    fun getSubscriptions(): Single<List<Subscription>> = billingRepository.getSubscriptions()

    fun showProductCheckout(activity: Activity, productId: String): Completable {
        return billingRepository.showProductCheckout(activity, productId)
    }

    fun observePurchase(): Observable<Optional<PurchaseEntity>> = purchaseRepository.observe()
        .flatMap { purchaseOptional ->
            when (purchaseOptional) {
                is Some -> verifyPurchaseUseCase.execute(purchaseOptional.value)
                    .toObservableDefault(Some(purchaseOptional.value))
                else -> None.observableJust()
            }
        }
}

/**
 * Implement your own class in your project completing or throwing error if the purchase is not valid
 */
interface VerifyPurchaseUseCase {

    fun execute(purchase: PurchaseEntity): Completable
}
