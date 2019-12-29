package com.marcohc.terminator.sample.features.detail

import com.marcohc.terminator.sample.data.model.Venue
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.VenueRepository
import io.reactivex.Scheduler
import io.reactivex.Single

internal class GetVenueByIdUseCase(
        private val connectionManager: ConnectionManager,
        private val venueRepository: VenueRepository,
        private val scheduler: Scheduler
) {

    fun execute(id: String): Single<Venue> {

        val getByIdObservable = if (connectionManager.isConnected()) {
            venueRepository.getByIdFromNetwork(id)
                .flatMap { venueFromNetwork ->
                    // Small adjustment to update full information including city
                    venueRepository.getByIdFromLocal(id)
                        .flatMap { localVenue ->
                            val venue = venueFromNetwork.copy(city = localVenue.city)
                            venueRepository.save(venue).toSingleDefault(venue)
                        }
                }
        } else {
            venueRepository.getByIdFromLocal(id)
        }

        return getByIdObservable
            .subscribeOn(scheduler)
    }
}

