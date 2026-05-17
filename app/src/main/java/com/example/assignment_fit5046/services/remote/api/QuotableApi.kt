package com.example.assignment_fit5046.services.remote.api

import com.example.assignment_fit5046.datamodels.Quote
import retrofit2.http.GET

interface QuotableApi {
    @GET("quotes/random")
    suspend fun getRandomQuote(): List<Quote>
}
