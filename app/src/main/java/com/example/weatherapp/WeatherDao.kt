package com.example.weatherapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_table")
    fun getAllCities(): Flow<List<CityWeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityWeatherEntity>)

    @Query("DELETE FROM weather_table")
    suspend fun clearCities()
}

// Тестовый комментарий для проверки Pull Request