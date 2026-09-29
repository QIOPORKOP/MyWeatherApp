package com.example.weatherapp

import kotlinx.coroutines.delay

// ПУНКТ 26: Контракт repository.
// Это правило: любой источник данных (БД, Сеть, Фейк) обязан уметь искать города.
interface WeatherRepository {
    suspend fun searchCities(query: String): List<CityWeather>
}

// ПУНКТ 27: Fake-реализация для тестов и разработки.
// Вынесли наши хардкод-города сюда из ViewModel.
class FakeWeatherRepository : WeatherRepository {
    private val allCities = listOf(
        CityWeather(name = "Актобе", country = "Казахстан", temp = 25),
        CityWeather(name = "Алматы", country = "Казахстан", temp = 22),
        CityWeather(name = "Астана", country = "Казахстан", temp = 18),
        CityWeather(name = "Москва", country = "Россия", temp = 20),
        CityWeather(name = "Лондон", country = "Великобритания", temp = 15)
    )

    override suspend fun searchCities(query: String): List<CityWeather> {
        delay(1000L) // Имитируем долгий ответ от сервера/сети (1 секунда)

        return if (query.isBlank()) {
            allCities
        } else {
            allCities.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    // Реальная реализация репозитория для работы с локальной базой данных Room
    class RoomWeatherRepository(private val weatherDao: WeatherDao) {

        // Получаем реактивный поток данных из локальной базы
        fun getCitiesFromDb(): kotlinx.coroutines.flow.Flow < List < CityWeatherEntity > > {
            return weatherDao.getAllCities()
        }

        // Сохраняем новые города в базу
        suspend fun saveCitiesToDb(cities: List < CityWeatherEntity > ) {
            weatherDao.insertCities(cities)
        }

        // Очищаем базу (пригодится для обновления данных с сети)
        suspend fun clearDb() {
            weatherDao.clearCities()
        }
    }
}