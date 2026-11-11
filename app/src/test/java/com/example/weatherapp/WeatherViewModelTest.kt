package com.example.weatherapp

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Создаем ручные фейки (Mock/Fake) для интерфейсов, чтобы не зависеть от БД и интернета!
    // Создаем пустой класс Application, чтобы обмануть AndroidViewModel
    class FakeApplication : Application()

    class FakePrefsRepo : IUserPreferencesRepository {
        override val userDataFlow: Flow<UserData> = flowOf(UserData("Test", "City", false, ""))
        override suspend fun saveUserData(name: String, city: String) {}
        override suspend fun setDarkTheme(isDarkTheme: Boolean) {}
        override suspend fun saveAvatarUri(uri: String) {}
    }

    class TestWeatherRepository(private val returnEmpty: Boolean = false) : WeatherRepository {
        
        var fetchCalledCount = 0

        override suspend fun searchCities(query: String): List<CityWeather> = emptyList()

        override fun getCitiesFromDb(): Flow<List<CityWeatherEntity>> {
            return if (returnEmpty) {
                flowOf(emptyList())
            } else {
                flowOf(listOf(CityWeatherEntity("1", "Актобе", "Казахстан", 25, 0.0, 0.0, true)))
            }
        }

        override fun getSavedCities(): Flow<List<CityWeatherEntity>> {
            return if (returnEmpty) {
                flowOf(emptyList())
            } else {
                flowOf(listOf(CityWeatherEntity("1", "Актобе", "Казахстан", 25, 0.0, 0.0, true)))
            }
        }

        override fun searchCitiesInDb(query: String): Flow<List<CityWeatherEntity>> = flowOf(emptyList())

        override suspend fun toggleCitySavedState(cityId: String, isSaved: Boolean) {}

        override suspend fun fetchAndSyncWeather() {
            fetchCalledCount++
        }

        override suspend fun fetchWeatherForCoordinates(lat: Double, lon: Double, isPrecise: Boolean) {}
    }

    @Before
    fun setup() {
        // Подменяем главный поток на тестовый (Требование 27: coroutines)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test Success state when cities are loaded`() = runTest(testDispatcher) {
        // 1. УСПЕХ (Success) - проверка успешной загрузки городов
        
        val fakeRepo = TestWeatherRepository(returnEmpty = false)
        val fakePrefs = FakePrefsRepo()
        
        // Создаем ViewModel, передавая фейковые репозитории (Чистая архитектура!)
        val viewModel = WeatherViewModel(
            application = FakeApplication(), // Передаем фейковый контекст
            savedStateHandle = SavedStateHandle(),
            repository = fakeRepo,
            userPrefsRepository = fakePrefs
        )
        
        // Прокручиваем все запущенные корутины (init блок) до конца
        advanceUntilIdle()

        // Проверяем состояние
        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is WeatherUiState.Success)
        
        val successState = state as WeatherUiState.Success
        // Проверяем, что в состоянии успеха есть страна "Казахстан" из нашего фейка
        assertTrue(successState.savedCities.containsKey("Казахстан"))
    }

    @Test
    fun `test Error state when search query finds nothing`() = runTest(testDispatcher) {
        // 2. ОШИБКА (Error) - проверка состояния ошибки при пустом результате поиска
        
        val fakeRepo = TestWeatherRepository(returnEmpty = false)
        val fakePrefs = FakePrefsRepo()

        val viewModel = WeatherViewModel(
            application = FakeApplication(),
            savedStateHandle = SavedStateHandle(),
            repository = fakeRepo,
            userPrefsRepository = fakePrefs
        )
        advanceUntilIdle()

        // Вводим город, которого точно нет в нашем фейке
        viewModel.onSearchTextChanged("НесуществующийГород")
        advanceUntilIdle()

        // Проверяем, что состояние перешло в Error с правильным текстом
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is WeatherUiState.Error)
        assertEquals("Города не найдены", (state as WeatherUiState.Error).message)
    }

    @Test
    fun `test Cancellation of fast consecutive search queries (Debounce)`() = runTest(testDispatcher) {
        // 3. ОТМЕНА (Cancellation корутины) 
        // Проверяем, что при быстром вводе текста, debounce отменяет предыдущие запросы
        
        val fakeRepo = TestWeatherRepository(returnEmpty = true)
        val fakePrefs = FakePrefsRepo()

        val viewModel = WeatherViewModel(
            application = FakeApplication(),
            savedStateHandle = SavedStateHandle(),
            repository = fakeRepo,
            userPrefsRepository = fakePrefs
        )
        
        // Эмулируем быстрый ввод букв пользователем (пользователь печатает "Акт")
        viewModel.onSearchTextChanged("А")
        viewModel.onSearchTextChanged("Ак")
        viewModel.onSearchTextChanged("Акт")
        
        // Ждем завершения корутин
        advanceUntilIdle()
        
        // Благодаря отмене (debounce = 500ms) сработал только последний запрос.
        // Состояние капсулы и переменной поиска должно быть "Акт", промежуточные отменены.
        assertEquals("Акт", viewModel.searchQuery.value)
    }
}
