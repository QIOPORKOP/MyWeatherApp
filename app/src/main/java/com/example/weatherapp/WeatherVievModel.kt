package com.example.weatherapp

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

data class CityWeather(
    val name: String,
    val country: String,
    val temp: Int,
    val id: String = UUID.randomUUID().toString(),
    val isLoading: Boolean = false, // Прокидываем флаг загрузки в UI
    val weatherCode: Int = 0 // Код погоды для отображения иконки
)

@Immutable
sealed interface WeatherUiState {
    object Loading : WeatherUiState
    // Здесь мы храним ТОЛЬКО сохраненные города (для главного экрана)
    data class Success(val savedCities: Map<String, List<CityWeather>>) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

// Отдельный стейт для результатов поиска
data class SearchResultState(
    val results: List<CityWeather> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(FlowPreview::class)
class WeatherViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val repository: WeatherRepository,
    private val userPrefsRepository: IUserPreferencesRepository
) : AndroidViewModel(application) {

    // Этот конструктор использует Android Studio по умолчанию
    constructor(application: Application, savedStateHandle: SavedStateHandle) : this(
        application,
        savedStateHandle,
        RealWeatherRepository(AppDatabase.getDatabase(application).weatherDao(), application), // Передаем application как Context
        UserPreferencesRepository(application)
    )

    // Ключ для сохранения данных
    private val SEARCH_QUERY_KEY = "search_query"

    // Достаем текст из капсулы (или пустую строку, если капсула пуста)
    private val _searchQuery = MutableStateFlow(savedStateHandle.get<String>(SEARCH_QUERY_KEY) ?: "")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    // Стейт для списка найденных городов (когда мы печатаем в поиске)
    private val _searchResults = MutableStateFlow<List<CityWeather>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // 2. Превращаем Flow из DataStore в StateFlow для удобной работы в Compose
    val userData = userPrefsRepository.userDataFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserData("", "", false, "")
    )

    init {
        // Запрашиваем реальную погоду из интернета при запуске (для обновления температуры)
        viewModelScope.launch {
            repository.fetchAndSyncWeather()
        }

        // 1. Подписываемся ТОЛЬКО на СОХРАНЕННЫЕ города (isSaved = true)
        viewModelScope.launch {
            repository.getSavedCities().collect { entities ->
                val savedCities = entities.map {
                    CityWeather(
                        name = it.name,
                        country = it.country,
                        temp = it.temp,
                        id = it.id,
                        isLoading = it.isLoading, // Читаем из базы!
                        weatherCode = it.weatherCode // Читаем из базы!
                    )
                }

                val processedData = savedCities
                    .groupBy { it.country }

                // Если список сохраненных городов пуст, возвращаем Success с пустой мапой
                // Если не пуст, возвращаем Success с данными
                _uiState.value = WeatherUiState.Success(processedData)
            }
        }

        // 2. Обработка живого поиска (из всей базы из 38 городов)
        viewModelScope.launch {
            _searchQuery
                .debounce(300.milliseconds)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList() // Если поиск пуст - прячем результаты
                    } else {
                        repository.getCitiesFromDb().collect { entities ->
                            val allCities = entities.filter { it.id != "my_location_id" }.map {
                                CityWeather(it.name, it.country, it.temp, it.id, it.isLoading, it.weatherCode)
                            }
                            
                            val qLen = query.trim().length
                            if (qLen <= 2) {
                                _searchResults.value = allCities.sortedByDescending { AppTranslator.matchesQuery(it.name, it.country, query) }
                            } else {
                                _searchResults.value = allCities.filter { AppTranslator.matchesQuery(it.name, it.country, query) }
                            }
                        }
                    }
                }
        }
    }

    fun onSearchTextChanged(newText: String?) {
        val text = newText ?: ""
        _searchQuery.value = text
        savedStateHandle[SEARCH_QUERY_KEY] = text
    }

    // Функция для свайпа (Swipe-to-Delete) и добавления
    fun toggleCitySavedState(cityId: String, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleCitySavedState(cityId, isSaved)
        }
    }

    // Функция для кнопки "Повторить попытку"
    fun retryLoading() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.fetchAndSyncWeather() 
        }
    }

    // 3. Функция сохранения профиля
    fun saveProfile(name: String, city: String) {
        viewModelScope.launch {
            userPrefsRepository.saveUserData(name, city)
        }
    }

    // 4. Функция переключения темы (Требование 13)
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setDarkTheme(isDark)
        }
    }

    // 5. Функция сохранения аватарки (Требование 26)
    fun saveAvatarUri(uri: String) {
        viewModelScope.launch {
            userPrefsRepository.saveAvatarUri(uri)
        }
    }

    // 6. Функция для получения погоды по GPS координатам (Требование 18)
    fun fetchWeatherForLocation(lat: Double, lon: Double, isPrecise: Boolean) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.fetchWeatherForCoordinates(lat, lon, isPrecise)
        }
    }
}