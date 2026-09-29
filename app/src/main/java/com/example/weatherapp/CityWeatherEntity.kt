package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class CityWeatherEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String,
    val temp: Int
)