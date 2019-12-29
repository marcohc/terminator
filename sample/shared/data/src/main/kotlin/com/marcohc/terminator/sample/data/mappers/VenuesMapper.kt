package com.marcohc.terminator.sample.data.mappers

import com.marcohc.terminator.sample.data.api.VenueEntity
import com.marcohc.terminator.sample.data.model.Venue

internal object VenuesMapper {

    fun mapEntitiesToModels(city: String = "", entities: List<VenueEntity>): List<Venue> {
        return entities.map {
            mapEntityToModel(city, it)
        }
    }

    fun mapEntityToModel(city: String = "", entity: VenueEntity): Venue {
        return Venue(
            entity.id,
            entity.name,
            entity.description ?: "",
            city,
            entity.location.address ?: "",
            entity.rating ?: 0.0,
            entity.contact?.formattedPhone ?: "",
            entity.bestPhoto?.getPhotoUrl() ?: entity.photos?.getFirstValidVenuePhotoUrl() ?: ""
        )
    }
}
