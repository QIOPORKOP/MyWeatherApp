package com.example.weatherapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherAppUiTest {

    // Правило для работы с Compose UI в тестах
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearchFormInput() {
        // 1. Проверка формы (ввод текста в строку поиска)
        var searchedText = ""

        composeTestRule.setContent {
            WeatherMainScreen(
                uiState = WeatherUiState.Success(emptyMap()), // Пустой успешный стейт
                searchQuery = "",
                searchResults = emptyList(),
                onSearchQueryChange = { searchedText = it },
                onRetryClick = {},
                onLocationFetch = { _, _, _ -> },
                onToggleCitySave = { _, _ -> },
                onCityClick = {},
                onProfileClick = {}
            )
        }

        // Находим поле ввода по Content Description (Адаптация для слепых из Требования 21)
        composeTestRule
            .onNodeWithContentDescription("Поле для ввода названия города")
            .performTextInput("Астана")

        // Проверяем, что State Hoisting работает и коллбек вызвался с правильным текстом
        assertEquals("Астана", searchedText)
    }

    @Test
    fun testListDisplaysCities() {
        // 2. Проверка списка (отображение карточек с погодой)
        val fakeData = mapOf(
            "Казахстан" to listOf(
                CityWeather(name = "Алматы", country = "Казахстан", temp = 22)
            )
        )

        composeTestRule.setContent {
            WeatherMainScreen(
                uiState = WeatherUiState.Success(fakeData), // Подсовываем фейковые данные
                searchQuery = "",
                searchResults = emptyList(),
                onSearchQueryChange = {},
                onRetryClick = {},
                onLocationFetch = { _, _, _ -> },
                onToggleCitySave = { _, _ -> },
                onCityClick = {},
                onProfileClick = {}
            )
        }

        // Проверяем, что заголовок страны и данные города физически отрисовались на экране
        composeTestRule.onNodeWithText("Казахстан").assertIsDisplayed()
        composeTestRule.onNodeWithText("Алматы").assertIsDisplayed()
        composeTestRule.onNodeWithText("+22°C").assertIsDisplayed()
    }

    @Test
    fun testNavigationToDetailsOnCardClick() {
        // 3. Переход на детали (клик по карточке и проверка открытия нового экрана)
        val fakeData = mapOf(
            "Казахстан" to listOf(
                CityWeather(name = "Алматы", country = "Казахстан", temp = 22)
            )
        )
        var clickedCity = ""

        composeTestRule.setContent {
            WeatherMainScreen(
                uiState = WeatherUiState.Success(fakeData),
                searchQuery = "",
                searchResults = emptyList(),
                onSearchQueryChange = {},
                onRetryClick = {},
                onLocationFetch = { _, _, _ -> },
                onToggleCitySave = { _, _ -> },
                // Этот коллбек дергает NavController на реальном устройстве.
                // В тесте мы просто перехватываем переданное имя города.
                onCityClick = { clickedCity = it },
                onProfileClick = {}
            )
        }

        // Шаг 1: Находим карточку города и кликаем, чтобы она раскрылась (анимация)
        composeTestRule.onNodeWithText("Алматы").performClick()

        // Шаг 2: В раскрытой карточке находим кнопку "Подробнее" и жмем на нее
        composeTestRule.onNodeWithText("Подробнее").assertIsDisplayed().performClick()

        // Проверяем, что навигация сработала с правильным аргументом
        assertEquals("Алматы", clickedCity)
    }
}
