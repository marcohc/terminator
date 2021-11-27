package com.marcohc.terminator.core.billing.domain

import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import com.marcohc.terminator.core.billing.data.repositories.ProductRepository
import io.reactivex.Single

class GetAvailableSubscriptions(private val repository: ProductRepository) {

    fun execute(): Single<List<ProductEntity>> = repository.getSubscriptions()
}
