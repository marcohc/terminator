package com.marcohc.terminator.core.billing

import com.marcohc.terminator.core.billing.data.BillingDatabase
import com.marcohc.terminator.core.billing.data.api.DevelopBillingApi
import com.marcohc.terminator.core.billing.data.api.GoogleBillingApi
import com.marcohc.terminator.core.billing.data.repositories.BillingRepositoryImpl
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepository
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepositoryImpl
import com.marcohc.terminator.core.billing.domain.DeleteAllPurchasesUseCase
import com.marcohc.terminator.core.billing.domain.DeleteAndSavePurchasesUseCase
import com.marcohc.terminator.core.billing.domain.DevelopVerifyPurchaseUseCase
import com.marcohc.terminator.core.koin.CoreModule
import org.koin.android.ext.koin.androidApplication
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

object BillingModule :
    CoreModule,
    KoinComponent {

    override val module = module {

        single {
            val configuration = getConfiguration()
            val api = if (configuration.debug) {
                DevelopBillingApi(
                    deleteAllPurchasesUseCase = DeleteAllPurchasesUseCase(repository = purchaseRepository),
                    deleteAndSavePurchasesUseCase = DeleteAndSavePurchasesUseCase(repository = purchaseRepository),
                )
            } else {
                GoogleBillingApi(
                    context = androidApplication(),
                    productIdsList = configuration.skuList,
                    deleteAllPurchasesUseCase = DeleteAllPurchasesUseCase(repository = purchaseRepository),
                    deleteAndSavePurchasesUseCase = DeleteAndSavePurchasesUseCase(repository = purchaseRepository),
                )
            }

            BillingUseCase(
                billingRepository = BillingRepositoryImpl(
                    api = api,
                    scheduler = get()
                ),
                purchaseRepository = purchaseRepository,
                verifyPurchaseUseCase = if (getConfiguration().debug) {
                    DevelopVerifyPurchaseUseCase()
                } else {
                    // Do not remove this type or it'll crash, you've been warned ;)
                    get<VerifyPurchaseUseCase>()
                },
            )
        }
    }

    // Do not expose data classes into Koin
    private val database: BillingDatabase by lazy { BillingDatabase.getInstance(context = get()) }
    private val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepositoryImpl(
            dao = database.purchaseDao(),
            scheduler = get()
        )
    }

    private fun Scope.getConfiguration() = getOrNull<BillingConfiguration>(named(CONFIGURATION))
        ?: throw IllegalStateException("Ey developer, you must declare $CONFIGURATION into Koin")

    const val CONFIGURATION = "BILLING_CONFIGURATION"
}
