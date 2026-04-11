package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

// OpenWeatherMap current weather API response
data class WeatherResponse(
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: WeatherMain,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("wind") val wind: WeatherWind
)

data class WeatherMain(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int
)

data class WeatherCondition(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WeatherWind(
    @SerializedName("speed") val speed: Double
)
