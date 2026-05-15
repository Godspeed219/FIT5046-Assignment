package com.example.assignment_fit5046.services.remote.api

import com.example.assignment_fit5046.datamodels.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("search")
    suspend fun geocode(
        @Query("q") address: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1
    ): List<GeocodingResponse>
}
