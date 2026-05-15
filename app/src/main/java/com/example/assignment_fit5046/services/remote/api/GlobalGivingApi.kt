package com.example.assignment_fit5046.services.remote.api

import com.example.assignment_fit5046.datamodels.NgoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GlobalGivingApi {
    @GET("api/public/projectservice/orgs/active.json")
    suspend fun searchOrganizations(
        @Query("api_key") apiKey: String,
        @Query("keyword") keyword: String = ""
    ): NgoSearchResponse
}
