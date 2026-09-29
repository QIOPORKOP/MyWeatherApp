package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ПУНКТ 20: Светлая и тёмная темы обрабатываются внутри WeatherAppTheme.
            // Цвет фона Scaffold теперь автоматически меняется на темный ночью.
            WeatherAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    WeatherMainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WeatherMainScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    // ПУНКТ 24: Подключить StateFlow через collectAsStateWithLifecycle
    // Это экономит ресурсы телефона, когда приложение свернуто
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ПУНКТ 23: Поднять состояние формы к вызывающему компоненту (State Hoisting).
        // Компонент не управляет сам собой, мы передаем ему searchQuery из ViewModel.
        WeatherSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchTextChanged(it) },
            focusManager = focusManager
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            }
            is WeatherUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
            is WeatherUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.groupedCities.forEach { (country, cities) ->
                        item {
                            Text(
                                text = country,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(items = cities, key = { city -> city.id }) { city ->
                            CityCard(city)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    val isError = query.length == 1

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Поиск города (Актобе, Москва...)") },
        isError = isError,
        supportingText = {
            if (isError) {
                Text("Введите минимум 2 символа")
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = modifier
            .fillMaxWidth()
            // ПУНКТ 21: Адаптировать для TalkBack.
            // Голосовой помощник прочитает эту подсказку для слепых пользователей.
            .semantics { contentDescription = "Поле для ввода названия города" },
        singleLine = true
    )
}

@Composable
fun CityCard(city: CityWeather) {
    // Состояние для анимации: раскрыта карточка или нет
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // ПУНКТ 21: Проверка TalkBack (объединяем чтение карточки в один блок)
            .semantics(mergeDescendants = true) {
                contentDescription = "Город ({city.name}, Температура){city.temp} градусов"
            }
            // Клик для раскрытия
            .clickable { expanded = !expanded }
            // ПУНКТ 22: Анимировать раскрытие карточки
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = city.name,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${city.temp}°C",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Если карточка раскрыта - показываем дополнительную анимационную часть
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Дополнительные данные загружаются...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "Ощущается как: +${city.temp - 2}°C",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

