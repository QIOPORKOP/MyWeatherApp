package com.example.weatherapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

data class CityWeather(
    val name: String,
    val country: String,
    val temp: Int,
    val id: String = UUID.randomUUID().toString()
)

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val groupedCities: Map<String, List<CityWeather>>) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

@OptIn(FlowPreview::class)
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FakeWeatherRepository.RoomWeatherRepository(database.weatherDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var currentDbCities: List<CityWeather> = emptyList()

    init {
        viewModelScope.launch {
            repository.getCitiesFromDb().collect { entities ->
                currentDbCities = entities.map {
                    CityWeather(
                        name = it.name,
                        country = it.country,
                        temp = it.temp,
                        id = it.id
                    )
                }

                if (currentDbCities.isEmpty()) {
                    val dummyData = listOf(
                        CityWeatherEntity(UUID.randomUUID().toString(), "Актобе", "Казахстан", 25),
                        CityWeatherEntity(UUID.randomUUID().toString(), "Алматы", "Казахстан", 22),
                        CityWeatherEntity(UUID.randomUUID().toString(), "Москва", "Россия", 20)
                    )
                    repository.saveCitiesToDb(dummyData)
                } else {
                    filterData(_searchQuery.value)
                }
            }
        }

        viewModelScope.launch {
            _searchQuery
                .debounce(500.milliseconds)
                .collect { query ->
                    filterData(query)
                }
        }
    }

    fun onSearchTextChanged(newText: String?) {
        _searchQuery.value = newText ?: ""
    }

    private fun filterData(query: String) {
        _uiState.value = WeatherUiState.Loading

        try {
            val filtered = if (query.isBlank()) {
                currentDbCities
            } else {
                currentDbCities.filter { it.name.contains(query, ignoreCase = true) }
            }

            val processedData = filtered
                .sortedByDescending { it.temp }
                .groupBy { it.country }

            if (processedData.isEmpty()) {
                _uiState.value = WeatherUiState.Error("Города не найдены")
            } else {
                _uiState.value = WeatherUiState.Success(processedData)
            }
        } catch (e: Exception) {
            _uiState.value = WeatherUiState.Error(e.message ?: "Ошибка фильтрации")
        }
    }
}
