package com.marcohc.terminator.core.billing.data.repositories

import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.marcohc.terminator.core.billing.data.dao.PurchaseDao
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import timber.log.Timber

interface PurchaseRepository {

    fun observe(): Observable<Optional<PurchaseEntity>>

    fun deleteAllAndSave(purchase: PurchaseEntity): Completable

    fun deleteAll(): Completable

}

internal class PurchaseRepositoryImpl(
        private val dao: PurchaseDao,
        private val scheduler: Scheduler
) : PurchaseRepository {

    override fun observe(): Observable<Optional<PurchaseEntity>> {

        val emptyPurchaseObservable = dao.observeCount()
            .toObservable()
            .filter { it == 0 }
            .map { None }
            .doOnNext { Timber.v("emptyObservable: $it") }

        val lastPurchaseObservable = dao.observeLimit1()
            .toObservable()
            .map { Some(it) }
            .doOnNext { Timber.v("lastPurchaseObservable: $it") }

        return Observable
            .merge(
                emptyPurchaseObservable,
                lastPurchaseObservable
            )
            .subscribeOn(scheduler)
    }

    override fun deleteAllAndSave(purchase: PurchaseEntity) = Completable
        .fromAction { dao.deleteAllAndSave(purchase) }
        .subscribeOn(scheduler)

    override fun deleteAll() = Completable
        .fromAction { dao.deleteAll() }
        .subscribeOn(scheduler)

}
