package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("display_name") val name: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String
)
