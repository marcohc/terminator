package com.marcohc.terminator.sample.data.repositories

import com.marcohc.terminator.sample.data.api.VenueApi
import com.marcohc.terminator.sample.data.dao.VenueDao
import com.marcohc.terminator.sample.data.mappers.VenuesMapper
import com.marcohc.terminator.sample.data.model.Venue
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

interface VenueRepository {

    fun getFromNetwork(city: String): Single<List<Venue>>
    fun getFromLocal(city: String): Single<List<Venue>>
    fun getByIdFromNetwork(id: String): Single<Venue>
    fun getByIdFromLocal(id: String): Single<Venue>
    fun save(venue: Venue):Completable
    fun saveAll(venues: List<Venue>):Completable

}

internal class VenueRepositoryImpl(
        private val api: VenueApi,
        private val dao: VenueDao,
        private val scheduler: Scheduler
) : VenueRepository {

    override fun getFromNetwork(city: String): Single<List<Venue>> {
        return api.getVenues(city)
            .map { response ->
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    when (body.meta.code) {
                        VenueApi.HTTP_CODE_SUCCESS -> VenuesMapper.mapEntitiesToModels(city, body.response.venues)
                        VenueApi.HTTP_CODE_BAD_REQUEST -> throw LocationUnknownException
                        else -> throw IllegalStateException(response.errorBody()?.string())
                    }
                } else {
                    throw LocationUnknownException
                }
            }
            .subscribeOn(scheduler)
    }

    override fun getFromLocal(city: String): Single<List<Venue>> {
        return Single
            .fromCallable { dao.getByCity(city) }
            .subscribeOn(scheduler)
    }

    override fun getByIdFromNetwork(id: String): Single<Venue> {
        return api.getVenueById(id)
            .map { response ->
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    when (body.meta.code) {
                        VenueApi.HTTP_CODE_SUCCESS -> VenuesMapper.mapEntityToModel(entity = body.response.venue)
                        VenueApi.HTTP_CODE_BAD_REQUEST -> throw LocationUnknownException
                        else -> throw IllegalStateException(response.errorBody()?.string())
                    }
                } else {
                    throw LocationUnknownException
                }
            }
            .subscribeOn(scheduler)
    }

    override fun getByIdFromLocal(id: String): Single<Venue> {
        return Single
            .fromCallable { dao.getById(id) ?: throw VenueUnknownException }
            .subscribeOn(scheduler)
    }

    override fun save(venue: Venue): Completable = Completable.fromAction { dao.save(venue) }

    override fun saveAll(venues: List<Venue>): Completable = Completable.fromAction { dao.saveAll(venues.toTypedArray()) }

}
