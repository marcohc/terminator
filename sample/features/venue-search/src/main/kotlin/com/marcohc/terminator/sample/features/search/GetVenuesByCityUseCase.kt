package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.sample.data.model.Venue
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.VenueRepository
import io.reactivex.Scheduler
import io.reactivex.Single

internal class GetVenuesByCityUseCase(
        private val connectionManager: ConnectionManager,
        private val venueRepository: VenueRepository,
        private val scheduler: Scheduler
) {

    fun execute(city: String): Single<List<Venue>> {

        val getByIdObservable = if (connectionManager.isConnected()) {
            venueRepository.getFromNetwork(city)
                .doOnSuccess { venueRepository.saveAll(it) }
        } else {
            venueRepository.getFromLocal(city)
        }

        return getByIdObservable
            .subscribeOn(scheduler)
    }
}
