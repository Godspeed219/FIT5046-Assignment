package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature2m: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double
) {
    val description: String get() = when (weatherCode) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Cloudy"
    }
}
