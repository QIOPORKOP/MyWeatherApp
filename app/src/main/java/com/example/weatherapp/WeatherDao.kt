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

    @Query("SELECT * FROM weather_table WHERE isSaved = 1")
    fun getSavedCities(): Flow<List<CityWeatherEntity>>

    @Query("SELECT * FROM weather_table WHERE name LIKE '%' || :searchQuery || '%' OR country LIKE '%' || :searchQuery || '%'")
    fun searchCities(searchQuery: String): Flow<List<CityWeatherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityWeatherEntity>)

    @Query("UPDATE weather_table SET isSaved = :isSaved WHERE id = :cityId")
    suspend fun updateCitySavedState(cityId: String, isSaved: Boolean)

    @Query("UPDATE weather_table SET `temp` = :temp, weatherCode = :weatherCode, isLoading = 0 WHERE id = :cityId")
    suspend fun updateCityWeather(cityId: String, temp: Int, weatherCode: Int)

    @Query("UPDATE weather_table SET isLoading = :isLoading WHERE id = :cityId")
    suspend fun updateCityLoadingState(cityId: String, isLoading: Boolean)

    @Query("SELECT COUNT(id) FROM weather_table")
    suspend fun getCitiesCount(): Int

    @Query("DELETE FROM weather_table")
    suspend fun clearCities()
}

// Тестовый комментарий для проверки Pull Request