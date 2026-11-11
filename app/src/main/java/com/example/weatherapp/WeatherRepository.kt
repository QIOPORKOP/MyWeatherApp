package com.example.weatherapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

// 1. Создаем "движок" для сети (Синглтон Retrofit)
object NetworkModule {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

// 2. Контракт: любой источник данных обязан уметь делать следующее
interface WeatherRepository {
    suspend fun searchCities(query: String): List<CityWeather>
    fun getCitiesFromDb(): Flow<List<CityWeatherEntity>>
    fun getSavedCities(): Flow<List<CityWeatherEntity>>
    fun searchCitiesInDb(query: String): Flow<List<CityWeatherEntity>>
    suspend fun fetchAndSyncWeather()
    suspend fun fetchWeatherForCoordinates(lat: Double, lon: Double, isPrecise: Boolean)
    suspend fun toggleCitySavedState(cityId: String, isSaved: Boolean)
}

// 3. Реальный репозиторий (Работает с БД и Сетью одновременно!)
class RealWeatherRepository(
    private val weatherDao: WeatherDao,
    private val context: Context, // Подключаем Context для SharedPreferences
    private val weatherApi: WeatherApi = NetworkModule.api // Подключаем API
) : WeatherRepository {

    private val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    private val LAST_UPDATE_KEY = "last_update_time"
    private val CACHE_DURATION_MS = 2 * 60 * 60 * 1000L // 2 часа

    // Золотой фонд городов (38 штук, 16 стран)
    private val initialCities = listOf(
        // Казахстан
        CityWeatherEntity("kz_1", "Астана", "Казахстан", 1, 51.16, 71.43, false),
        CityWeatherEntity("kz_2", "Алматы", "Казахстан", 1, 43.25, 76.95, false),
        CityWeatherEntity("kz_3", "Шымкент", "Казахстан", 1, 42.30, 69.60, false),
        // Россия
        CityWeatherEntity("ru_1", "Москва", "Россия", 1, 55.75, 37.62, false),
        CityWeatherEntity("ru_2", "Санкт-Петербург", "Россия", 1, 59.93, 30.31, false),
        CityWeatherEntity("ru_3", "Новосибирск", "Россия", 1, 55.03, 82.92, false),
        // Узбекистан
        CityWeatherEntity("uz_1", "Ташкент", "Узбекистан", 1, 41.31, 69.28, false),
        CityWeatherEntity("uz_2", "Самарканд", "Узбекистан", 1, 39.65, 66.97, false),
        // ОАЭ
        CityWeatherEntity("ae_1", "Дубай", "ОАЭ", 1, 25.21, 55.27, false),
        CityWeatherEntity("ae_2", "Абу-Даби", "ОАЭ", 1, 24.45, 54.37, false),
        // США
        CityWeatherEntity("us_1", "Нью-Йорк", "США", 1, 40.71, -74.01, false),
        CityWeatherEntity("us_2", "Лос-Анджелес", "США", 1, 34.05, -118.24, false),
        CityWeatherEntity("us_3", "Чикаго", "США", 1, 41.85, -87.65, false),
        // Турция
        CityWeatherEntity("tr_1", "Стамбул", "Турция", 1, 41.01, 28.97, false),
        CityWeatherEntity("tr_2", "Анкара", "Турция", 1, 39.92, 32.85, false),
        CityWeatherEntity("tr_3", "Анталья", "Турция", 1, 36.90, 30.70, false),
        // Германия
        CityWeatherEntity("de_1", "Берлин", "Германия", 1, 52.52, 13.40, false),
        CityWeatherEntity("de_2", "Мюнхен", "Германия", 1, 48.14, 11.58, false),
        // Китай
        CityWeatherEntity("cn_1", "Пекин", "Китай", 1, 39.90, 116.41, false),
        CityWeatherEntity("cn_2", "Шанхай", "Китай", 1, 31.22, 121.46, false),
        CityWeatherEntity("cn_3", "Гуанчжоу", "Китай", 1, 23.13, 113.26, false),
        // Австралия
        CityWeatherEntity("au_1", "Сидней", "Австралия", 1, -33.87, 151.21, false),
        CityWeatherEntity("au_2", "Мельбурн", "Австралия", 1, -37.81, 144.96, false),
        // Испания
        CityWeatherEntity("es_1", "Мадрид", "Испания", 1, 40.42, -3.70, false),
        CityWeatherEntity("es_2", "Барселона", "Испания", 1, 41.38, 2.16, false),
        // Бразилия
        CityWeatherEntity("br_1", "Рио-де-Жанейро", "Бразилия", 1, -22.91, -43.21, false),
        CityWeatherEntity("br_2", "Сан-Паулу", "Бразилия", 1, -23.55, -46.63, false),
        // Канада
        CityWeatherEntity("ca_1", "Торонто", "Канада", 1, 43.70, -79.42, false),
        CityWeatherEntity("ca_2", "Ванкувер", "Канада", 1, 49.25, -123.12, false),
        // Италия
        CityWeatherEntity("it_1", "Рим", "Италия", 1, 41.89, 12.51, false),
        CityWeatherEntity("it_2", "Милан", "Италия", 1, 45.46, 9.19, false),
        // Вьетнам
        CityWeatherEntity("vn_1", "Ханой", "Вьетнам", 1, 21.02, 105.84, false),
        CityWeatherEntity("vn_2", "Хошимин", "Вьетнам", 1, 10.82, 106.63, false),
        // Грузия
        CityWeatherEntity("ge_1", "Тбилиси", "Грузия", 1, 41.69, 44.83, false),
        CityWeatherEntity("ge_2", "Батуми", "Грузия", 1, 41.64, 41.63, false),
        // Таиланд
        CityWeatherEntity("th_1", "Бангкок", "Таиланд", 1, 13.75, 100.52, false),
        CityWeatherEntity("th_2", "Пхукет", "Таиланд", 1, 7.89, 98.40, false)
    )

    suspend fun populateInitialDataIfNeeded() {
        if (weatherDao.getCitiesCount() < 38) {
            weatherDao.insertCities(initialCities)
        }
    }

    override suspend fun fetchAndSyncWeather() {
        populateInitialDataIfNeeded()

        // Проверка кэша по времени (2 часа)
        val lastUpdateTime = prefs.getLong(LAST_UPDATE_KEY, 0L)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < CACHE_DURATION_MS) {
            Log.d("WeatherApp", "Cache is still valid. Skipping network request.")
            return // В сеть не идем, используем данные из Room
        }

        // ИСПРАВЛЕНИЕ ТУТ: Берем ВСЕ 38 городов из базы!
        val allCities = weatherDao.getAllCities().first()
        if (allCities.isEmpty()) return

        try {
            // Собираем координаты всех 38 городов в строчки через запятую
            val latString = allCities.joinToString(",") { it.lat.toString() }
            val lonString = allCities.joinToString(",") { it.lon.toString() }

            // Делаем 1 выстрел по Batch API
            val rawResponse = weatherApi.getWeatherBatch(lat = latString, lon = lonString)

            if (rawResponse.isJsonArray) {
                // Сервер вернул массив из 38 ответов
                val jsonArray = rawResponse.asJsonArray
                // Пробегаемся по всем нашим городам и обновляем температуру
                allCities.forEachIndexed { index, city ->
                    if (index < jsonArray.size()) {
                        val cityJson = jsonArray[index].asJsonObject
                        val currentObj = cityJson.getAsJsonObject("current_weather")
                        if (currentObj != null) {
                            val rawTemp = currentObj.get("temperature").asDouble
                            val temp = if (rawTemp >= 0.0 && Math.round(rawTemp).toInt() == 0) 1
                                       else if (rawTemp < 0.0 && Math.round(rawTemp).toInt() == 0) -1
                                       else Math.round(rawTemp).toInt()
                            val code = currentObj.get("weathercode").asInt
                            weatherDao.updateCityWeather(city.id, temp, code)
                        }
                    }
                }
            } else if (rawResponse.isJsonObject) {
                val jsonObj = rawResponse.asJsonObject
                val currentObj = jsonObj.getAsJsonObject("current_weather")
                if (currentObj != null) {
                    val rawTemp = currentObj.get("temperature").asDouble
                    val temp = if (rawTemp >= 0.0 && Math.round(rawTemp).toInt() == 0) 1
                               else if (rawTemp < 0.0 && Math.round(rawTemp).toInt() == 0) -1
                               else Math.round(rawTemp).toInt()
                    val code = currentObj.get("weathercode").asInt
                    weatherDao.updateCityWeather(allCities[0].id, temp, code)
                }
            }
            
            // Если запрос прошел успешно, записываем новое время обновления
            prefs.edit().putLong(LAST_UPDATE_KEY, currentTime).apply()
            
        } catch (e: Exception) {
            Log.e("WeatherApp", "Failed to fetch batch weather", e)
        }
    }

    override fun getCitiesFromDb(): Flow<List<CityWeatherEntity>> {
        return weatherDao.getAllCities()
    }

    override fun getSavedCities(): Flow<List<CityWeatherEntity>> {
        return weatherDao.getSavedCities()
    }

    override fun searchCitiesInDb(query: String): Flow<List<CityWeatherEntity>> {
        return weatherDao.searchCities(query)
    }

    override suspend fun toggleCitySavedState(cityId: String, isSaved: Boolean) {
        weatherDao.updateCitySavedState(cityId, isSaved)
    }

    suspend fun saveCitiesToDb(cities: List<CityWeatherEntity>) {
        weatherDao.insertCities(cities)
    }

    suspend fun clearDb() {
        weatherDao.clearCities()
    }

    override suspend fun searchCities(query: String): List<CityWeather> {
        return emptyList() // Не используется в нашей архитектуре
    }

    // НОВАЯ ФУНКЦИЯ: Качаем реальную погоду и синхронизируем с БД (Требование 25)
    // Удалили старую реализацию fetchAndSyncWeather

    // НОВАЯ ФУНКЦИЯ: Скачиваем погоду по конкретным координатам GPS (Требование 18)
    override suspend fun fetchWeatherForCoordinates(lat: Double, lon: Double, isPrecise: Boolean) {
        try {
            val response = weatherApi.getWeather(lat = lat, lon = lon)
            val rawTemp = response.currentWeather.temperature
            val realTemp = if (rawTemp >= 0.0 && Math.round(rawTemp).toInt() == 0) 1
                           else if (rawTemp < 0.0 && Math.round(rawTemp).toInt() == 0) -1
                           else Math.round(rawTemp).toInt()
            val weatherCode = response.currentWeather.weatherCode

            // Формируем динамическое название
            val cityName = if (isPrecise) "Моя локация" else "Приблизительная местность"

            // Создаем сущность
            val myLocationCity = CityWeatherEntity(
                id = "my_location_id", // Фиксированный ID, чтобы перезаписывать старую локацию
                name = cityName,
                country = "Текущее место",
                temp = realTemp,
                lat = lat,
                lon = lon,
                isSaved = true, // Моя локация всегда должна отображаться
                weatherCode = weatherCode
            )

            // Добавляем в базу
            saveCitiesToDb(listOf(myLocationCity))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// 4. Тот самый Fake (вынесли отдельно, чтобы не мешал, пригодится для тестов)
class FakeWeatherRepository : WeatherRepository {
    private val allCities = listOf(
        CityWeatherEntity("kz_1", "Астана", "Казахстан", 0, 51.16, 71.43, false),
        CityWeatherEntity("ru_1", "Москва", "Россия", 0, 55.75, 37.62, false)
    )

    override suspend fun searchCities(query: String): List<CityWeather> {
        return emptyList()
    }

    override fun getCitiesFromDb(): Flow<List<CityWeatherEntity>> {
        return flowOf(allCities.map {
            CityWeatherEntity(it.id, it.name, it.country, it.temp, 0.0, 0.0, true) 
        })
    }
    
    override fun getSavedCities(): Flow<List<CityWeatherEntity>> = getCitiesFromDb()
    override fun searchCitiesInDb(query: String): Flow<List<CityWeatherEntity>> = getCitiesFromDb()
    override suspend fun toggleCitySavedState(cityId: String, isSaved: Boolean) {}

    // Фейк не ходит в сеть (Для тестирования)
    override suspend fun fetchAndSyncWeather() {}
    override suspend fun fetchWeatherForCoordinates(lat: Double, lon: Double, isPrecise: Boolean) {}
}