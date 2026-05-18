package com.example.assignment_fit5046.services.remote.api

import com.example.assignment_fit5046.datamodels.ProjectSearchResponse
import com.example.assignment_fit5046.datamodels.SearchProjectsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GlobalGivingApi {
    @GET("api/public/projectservice/themes/{theme}/projects/active")
    suspend fun getProjectsByTheme(
        @Path("theme") theme: String,
        @Query("api_key") apiKey: String
    ): ProjectSearchResponse

    @GET("api/public/services/search/projects")
    suspend fun searchProjects(
        @Query("api_key") apiKey: String,
        @Query("q") keyword: String
    ): SearchProjectsResponse

}
