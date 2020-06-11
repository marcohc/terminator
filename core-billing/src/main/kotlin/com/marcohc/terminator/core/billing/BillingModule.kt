package com.marcohc.terminator.core.billing

import com.marcohc.terminator.core.billing.data.BillingDatabase
import com.marcohc.terminator.core.billing.data.api.DevelopBillingApi
import com.marcohc.terminator.core.billing.data.api.GoogleBillingApi
import com.marcohc.terminator.core.billing.data.repositories.ProductRepository
import com.marcohc.terminator.core.billing.data.repositories.ProductRepositoryImpl
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepository
import com.marcohc.terminator.core.billing.data.repositories.PurchaseRepositoryImpl
import com.marcohc.terminator.core.billing.domain.GetAvailableSubscriptions
import com.marcohc.terminator.core.billing.domain.ObservePurchaseUseCase
import com.marcohc.terminator.core.billing.domain.PurchaseEventBus
import com.marcohc.terminator.core.billing.domain.PurchaseSubscriptionUseCase
import com.marcohc.terminator.core.billing.domain.VerifyPurchaseUseCase
import com.marcohc.terminator.core.billing.domain.internal.DeleteAllPurchasesUseCase
import com.marcohc.terminator.core.billing.domain.internal.DeleteAndSavePurchasesUseCase
import com.marcohc.terminator.core.billing.domain.internal.DevelopVerifyPurchaseUseCase
import com.marcohc.terminator.core.billing.domain.internal.SaveProductsUseCase
import com.marcohc.terminator.core.koin.CoreModule
import org.koin.android.ext.koin.androidApplication
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

object BillingModule : CoreModule,
                       KoinComponent {

    override val module = module {

        factory { GetAvailableSubscriptions(repository = productRepository) }

        factory {
            ObservePurchaseUseCase(
                verifyPurchaseUseCase = if (getConfiguration().debug) {
                    DevelopVerifyPurchaseUseCase()
                } else {
                    // Do not remove this type or it'll crash, you've been warned ;)
                    get<VerifyPurchaseUseCase>()
                },
                repository = purchaseRepository
            )
        }

        factory {
            PurchaseSubscriptionUseCase(
                billingApi = get(),
                productRepository = productRepository
            )
        }

        single { PurchaseEventBus() }

        single {
            val configuration = getConfiguration()
            if (configuration.debug) {
                DevelopBillingApi(
                    skuList = configuration.skuList,
                    saveProductsUseCase = SaveProductsUseCase(repository = productRepository),
                    deleteAllPurchasesUseCase = DeleteAllPurchasesUseCase(repository = purchaseRepository),
                    deleteAndSavePurchasesUseCase = DeleteAndSavePurchasesUseCase(repository = purchaseRepository),
                    purchaseEventBus = get()
                )
            } else {
                GoogleBillingApi(
                    context = androidApplication(),
                    skuList = configuration.skuList,
                    saveProductsUseCase = SaveProductsUseCase(repository = productRepository),
                    deleteAllPurchasesUseCase = DeleteAllPurchasesUseCase(repository = purchaseRepository),
                    deleteAndSavePurchasesUseCase = DeleteAndSavePurchasesUseCase(repository = purchaseRepository),
                    purchaseEventBus = get()
                )
            }
        }
    }

    // Do not expose data classes into Koin
    private val database: BillingDatabase by lazy { BillingDatabase.getInstance(context = get()) }
    private val purchaseRepository: PurchaseRepository by lazy { PurchaseRepositoryImpl(dao = database.purchaseDao(), scheduler = get()) }
    private val productRepository: ProductRepository by lazy { ProductRepositoryImpl(dao = database.productDao(), scheduler = get()) }
    private fun Scope.getConfiguration() = getOrNull<BillingConfiguration>(named(CONFIGURATION)) ?: throw IllegalStateException("Ey developer, you must declare BillingConfiguration into Koin")

    const val CONFIGURATION = "BILLING_CONFIGURATION"

}

