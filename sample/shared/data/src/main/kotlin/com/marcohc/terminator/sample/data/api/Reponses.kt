package com.marcohc.terminator.sample.data.api

internal data class Location(
        val address: String? = "",
        val lat: Double,
        val lng: Double
)

internal data class Contact(
        val phone: String? = "",
        val formattedPhone: String? = ""
)

internal data class Photo(
        val prefix: String,
        val suffix: String
) {
    private companion object {
        private const val FIX_SIZE = "500x500"
    }

    fun getPhotoUrl(): String {
        return "$prefix$FIX_SIZE$suffix"
    }
}

internal data class Groups(
        val type: String? = "",
        val count: Int? = 0,
        val items: List<Photo>
)

internal data class PhotoResponse(
        val count: Int,
        val groups: List<Groups>
) {
    private companion object {
        private const val TYPE_VENUE = "venue"
    }

    fun getFirstValidVenuePhotoUrl(): String {
        val photo = groups.findLast { it.type == TYPE_VENUE }?.items?.firstOrNull()
        return photo?.getPhotoUrl() ?: ""
    }
}

internal data class VenueEntity(
        val id: String,
        val name: String,
        val location: Location,
        val contact: Contact? = null,
        val photos: PhotoResponse? = null,
        val bestPhoto: Photo? = null,
        val description: String? = "",
        val rating: Double? = 0.0
)

internal data class GetVenueByIdResponse(
        val venue: VenueEntity
)

internal data class GetVenuesResponse(
        val venues: List<VenueEntity>
)

internal data class Metadata(
        val code: Int
)

internal class GetVenuesParentResponse(
        val meta: Metadata,
        val response: GetVenuesResponse
)

internal class GetVenueByIdParentResponse(
        val meta: Metadata,
        val response: GetVenueByIdResponse
)
