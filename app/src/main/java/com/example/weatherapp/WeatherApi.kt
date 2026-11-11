package com.example.weatherapp

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.JsonObject

// 1. Классы данных для расшифровки JSON от Open-Meteo
data class CurrentWeather(
    @SerializedName("temperature")
    val temperature: Double,
    @SerializedName("windspeed")
    val windSpeed: Double,
    @SerializedName("weathercode")
    val weatherCode: Int
)

// 2. Сам интерфейс для Retrofit
interface WeatherApi {
    
    // Единичный запрос (например, для локации по GPS)
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): SingleWeatherResponse

    // Массовый запрос (Batch API)
    @GET("v1/forecast")
    suspend fun getWeatherBatch(
        @Query("latitude", encoded = true) lat: String,
        @Query("longitude", encoded = true) lon: String,
        @Query("current_weather") currentWeather: Boolean = true
    ): JsonElement
}

data class SingleWeatherResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather
)