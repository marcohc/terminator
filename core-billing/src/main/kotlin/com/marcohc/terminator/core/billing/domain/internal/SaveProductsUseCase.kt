package com.marcohc.terminator.core.billing.domain.internal

import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import com.marcohc.terminator.core.billing.data.repositories.ProductRepository

internal class SaveProductsUseCase(private val repository: ProductRepository) {

    fun execute(products: List<ProductEntity>) = repository.saveAll(products)
}
