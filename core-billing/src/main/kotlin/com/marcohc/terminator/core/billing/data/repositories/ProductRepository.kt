package com.marcohc.terminator.core.billing.data.repositories

import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.marcohc.terminator.core.billing.data.dao.ProductDao
import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

interface ProductRepository {

    fun getSubscriptions(): Single<List<ProductEntity>>

    fun getById(id: String): Single<Optional<ProductEntity>>

    fun saveAll(productsList: List<ProductEntity>): Completable
}

internal class ProductRepositoryImpl(
    private val dao: ProductDao,
    private val scheduler: Scheduler
) : ProductRepository {

    override fun getSubscriptions(): Single<List<ProductEntity>> = dao
        .getSubscriptions()
        .onErrorReturn { emptyList() }
        .subscribeOn(scheduler)

    override fun getById(id: String) = dao
        .getById(id)
        .map<Optional<ProductEntity>> { Some(it) }
        .onErrorReturn { None }
        .subscribeOn(scheduler)

    override fun saveAll(productsList: List<ProductEntity>) = dao
        .saveAll(productsList)
        .subscribeOn(scheduler)
}
