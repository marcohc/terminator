package com.marcohc.terminator.sample.data.api

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface VenueApi {

    @GET("venues/search")
    fun getVenues(
            @Query(PARAM_NEAR) query: String,
            @Query(PARAM_RADIUS) radius: Int = RADIUS,
            @Query(PARAM_LIMIT) limit: Int = LIMIT,
            @Query(PARAM_CLIENT_ID) clientId: String = CLIENT_ID,
            @Query(PARAM_CLIENT_SECRET) clientSecret: String = CLIENT_SECRET,
            @Query(PARAM_VERSION) version: String = CLIENT_VERSION
    ): Single<Response<GetVenuesParentResponse>>

    @GET("venues/{$PARAM_VENUE_ID}")
    fun getVenueById(
            @Path(PARAM_VENUE_ID) venueId: String,
            @Query(PARAM_RADIUS) radius: Int = RADIUS,
            @Query(PARAM_LIMIT) limit: Int = LIMIT,
            @Query(PARAM_CLIENT_ID) clientId: String = CLIENT_ID,
            @Query(PARAM_CLIENT_SECRET) clientSecret: String = CLIENT_SECRET,
            @Query(PARAM_VERSION) version: String = CLIENT_VERSION
    ): Single<Response<GetVenueByIdParentResponse>>

    companion object {
        const val HTTP_CODE_BAD_REQUEST = 400
        const val HTTP_CODE_SUCCESS = 200
        private const val CLIENT_ID = "DZAXLBVEOXFVPIVFOBZUV1N4TYK2MIJ00BCTWRNFAWOUBSFC"
        private const val CLIENT_SECRET = "04PJLUICQCT421B5XNZM4UYVNQIEGPCWPOJXO3MIDXKW2R1K"
        private const val CLIENT_VERSION = "20180929"
        private const val RADIUS = 1000
        private const val LIMIT = 10

        private const val PARAM_NEAR = "near"
        private const val PARAM_RADIUS = "radius"
        private const val PARAM_LIMIT = "limit"
        private const val PARAM_CLIENT_ID = "client_id"
        private const val PARAM_CLIENT_SECRET = "client_secret"
        private const val PARAM_VERSION = "v"
        private const val PARAM_VENUE_ID = "venueId"
    }

}

