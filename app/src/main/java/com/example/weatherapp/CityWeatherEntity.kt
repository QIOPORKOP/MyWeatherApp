package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class CityWeatherEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String,
    val temp: Int,
    val lat: Double,       // Широта для API
    val lon: Double,       // Долгота для API
    val isSaved: Boolean,  // Сохранен ли город на главном экране
    val isLoading: Boolean = false, // Флаг для UI: идет ли сейчас загрузка погоды
    val weatherCode: Int = 0 // Код погоды (0 - ясно, 61 - дождь и т.д.)
)