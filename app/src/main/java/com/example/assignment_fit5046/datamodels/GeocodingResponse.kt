package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
)
