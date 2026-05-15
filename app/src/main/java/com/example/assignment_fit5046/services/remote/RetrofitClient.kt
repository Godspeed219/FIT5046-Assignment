package com.example.assignment_fit5046.services.remote

import com.example.assignment_fit5046.services.remote.api.GeocodingApi
import com.example.assignment_fit5046.services.remote.api.GlobalGivingApi
import com.example.assignment_fit5046.services.remote.api.QuotableApi
import com.example.assignment_fit5046.services.remote.api.WeatherApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val quotableApi: QuotableApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.quotable.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuotableApi::class.java)
    }

    val weatherApi: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "VolunteerLink/1.0")
                    .build()
                chain.proceed(request)
            }
            .build()
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApi::class.java)
    }

    val globalGivingApi: GlobalGivingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.globalgiving.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GlobalGivingApi::class.java)
    }
}
