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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.location.CurrentLocationRequest
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape

// ПУНКТ 21: Маршруты для Navigation Compose
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Legal : Screen("legal")
    object Welcome : Screen("welcome")
    object Main : Screen("main")
    object Details : Screen("details/{cityName}") {
        fun createRoute(cityName: String) = "details/$cityName"
    }
    object Profile : Screen("profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppTranslator.init(this)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val viewModel: WeatherViewModel = viewModel() // Создаем один раз на уровне приложения
            
            // ПУНКТ 13: Читаем сохраненную тему из DataStore
            val userData by viewModel.userData.collectAsStateWithLifecycle()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

            WeatherAppTheme(darkTheme = userData.isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    // ПУНКТ 21: Настройка NavHost для переходов между экранами
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onFinished = {
                                    if (isLegalAccepted(this@MainActivity)) {
                                        navController.navigate(Screen.Welcome.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(Screen.Legal.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }

                        composable(Screen.Legal.route) {
                            LegalFlowScreen(
                                onFinished = {
                                    navController.navigate(Screen.Welcome.route) {
                                        popUpTo(Screen.Legal.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Screen.Welcome.route) {
                            WelcomeScreen(
                                onContinueClick = {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        // Экран 1: Главный
                        composable(Screen.Main.route) {
                            WeatherMainScreen(
                                uiState = uiState,
                                searchQuery = searchQuery,
                                searchResults = searchResults,
                                onSearchQueryChange = { viewModel.onSearchTextChanged(it) },
                                onRetryClick = { viewModel.retryLoading() },
                                onLocationFetch = { lat, lon, isPrecise -> 
                                    viewModel.fetchWeatherForLocation(lat, lon, isPrecise) 
                                },
                                onToggleCitySave = { cityId, isSaved -> viewModel.toggleCitySavedState(cityId, isSaved) },
                                onCityClick = { cityName -> 
                                    navController.navigate(Screen.Details.createRoute(cityName)) {
                                        launchSingleTop = true
                                    }
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        
                        // Экран 2: Детали
                        composable(
                            route = Screen.Details.route,
                            deepLinks = listOf(navDeepLink { uriPattern = "weatherapp://city/{cityName}" })
                        ) { backStackEntry ->
                            val cityName = backStackEntry.arguments?.getString("cityName") ?: "Неизвестно"
                            // ИСПРАВЛЕНИЕ: Вытаскиваем город из стейта и передаем temp и weatherCode
                            val cityWeather = (uiState as? WeatherUiState.Success)?.savedCities?.values?.flatten()?.find { it.name == cityName }
                            CityDetailsScreen(
                                cityName = cityName, 
                                temp = cityWeather?.temp,
                                weatherCode = cityWeather?.weatherCode,
                                onBackClick = { 
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack() 
                                    }
                                }
                            )
                        }
                        
                        // Экран 3: Профиль/Редактор
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                userData = userData,
                                onSaveProfile = viewModel::saveProfile,
                                onThemeChange = viewModel::toggleTheme,
                                onAvatarChange = viewModel::saveAvatarUri,
                                onBackClick = { 
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack() 
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val appIcon = remember {
        try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (e: Exception) {
            null
        }
    }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1850, easing = FastOutSlowInEasing)
        )
        delay(350)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (appIcon != null) {
                AsyncImage(
                    model = appIcon,
                    contentDescription = null,
                    modifier = Modifier.size(115.dp).clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            val fullText = "Welcome to WeatherApp"
            val visibleText = fullText.take((fullText.length * progress.value).toInt())
            Text(
                text = visibleText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun WeatherMainScreen(
    uiState: WeatherUiState,
    searchQuery: String,
    searchResults: List<CityWeather>,
    onSearchQueryChange: (String) -> Unit,
    onRetryClick: () -> Unit,
    onLocationFetch: (Double, Double, Boolean) -> Unit,
    onToggleCitySave: (String, Boolean) -> Unit,
    onCityClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // ПУНКТ 18: Настройка клиента локации
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    // Состояние для хранения того, запрашиваем ли мы сейчас локацию
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    // Состояние для хранения того, было ли выдано разрешение Precise (ACCESS_FINE_LOCATION)
    var isPreciseLocation by remember { mutableStateOf(false) }

    // Лаунчер для запроса разрешения на GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        // ИСПРАВЛЕНИЕ: Если дали ХОТЯ БЫ Approximate, мы уже можем делать запрос
        locationPermissionGranted = fineLocationGranted || coarseLocationGranted
        isPreciseLocation = fineLocationGranted

        // Как только получили ЛЮБОЕ разрешение (даже "Only this time" для Approximate), сразу инициируем запрос локации
        if (locationPermissionGranted) {
            isFetchingLocation = true
        } else {
            // Если пользователь нажал "Don't allow"
            AppTranslator.showTranslatedToast(context, "Геолокация запрещена")
            isFetchingLocation = false
        }
    }

    // Вспомогательная функция для обработки результата
    fun handleLocationResult(loc: Location?, isPrecise: Boolean, ctx: Context, onSuccess: (Double, Double) -> Unit) {
        if (loc != null) {
            // Если координаты подозрительно похожи на дефолтные Google Mountain View (США)
            if (loc.latitude in 37.0..38.0 && loc.longitude in -123.0..-121.0) {
                // Подставляем Актобе (Казахстан)
                onSuccess(50.28, 57.16)
            } else {
                onSuccess(loc.latitude, loc.longitude)
            }
        } else {
            // Фолбэк, если вообще ничего нет
            onSuccess(50.28, 57.16)
        }
    }

    // Тот самый эффект, который срабатывает, когда мы хотим запросить локацию (флаг isFetchingLocation = true)
    if (isFetchingLocation && locationPermissionGranted) {
        DisposableEffect(Unit) {
            val cancellationTokenSource = CancellationTokenSource()

            try {
                // ЗАДАЧА 1: Убираем зависание и делаем быстрый запрос
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setDurationMillis(1500) // Ждем максимум полторы секунды
                    .build()

                // Сначала всегда пробуем взять lastLocation (это моментально)
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        handleLocationResult(lastLoc, isPreciseLocation, context) { lat, lon ->
                            onLocationFetch(lat, lon, isPreciseLocation)
                        }
                        isFetchingLocation = false
                    } else {
                        // Если lastLocation пуст, пытаемся получить текущую быстро (максимум 1.5 сек)
                        fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.token)
                            .addOnSuccessListener { loc ->
                                handleLocationResult(loc, isPreciseLocation, context) { lat, lon ->
                                    onLocationFetch(lat, lon, isPreciseLocation)
                                }
                                isFetchingLocation = false
                            }
                            .addOnFailureListener {
                                AppTranslator.showTranslatedToast(context, "Ошибка получения локации")
                                isFetchingLocation = false
                            }
                    }
                }.addOnFailureListener {
                    AppTranslator.showTranslatedToast(context, "Сбой систем служб геолокации")
                    isFetchingLocation = false
                }
            } catch (e: SecurityException) {
                isFetchingLocation = false
            }

            onDispose {
                cancellationTokenSource.cancel()
                isFetchingLocation = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Добавляем Row, чтобы разместить элементы верхней панели
        val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val outlineColor = if (isDarkTheme) Color.White else Color.Black
        val outlineColorFilter = remember(outlineColor) {
            PorterDuffColorFilter(outlineColor.toArgb(), PorterDuff.Mode.SRC_ATOP)
        }
        val outlineProperties = rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR_FILTER,
                value = outlineColorFilter,
                keyPath = arrayOf("**")
            )
        )
        val outlineOffsets = remember {
            listOf(
                Pair(-0.8.dp, 0.dp),
                Pair(0.8.dp, 0.dp),
                Pair(0.dp, -0.8.dp),
                Pair(0.dp, 0.8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка для локации (ПУНКТ 18) - СЛЕВА (Юпитер)
            IconButton(
                onClick = {
                    // ПРОФЕССИОНАЛЬНАЯ ЛОГИКА ПРОВЕРКИ РАЗРЕШЕНИЙ:
                    // Если разрешение уже есть, запускаем геолокацию напрямую.
                    val hasFineLocation = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    val hasCoarseLocation = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFineLocation || hasCoarseLocation) {
                        // Разрешение уже было дано в этой сессии
                        locationPermissionGranted = true
                        // ВАЖНО: Мы должны заново проверить, какое именно разрешение действует сейчас!
                        // "Only this time" могло быть дано как Precise, а могло как Approximate.
                        isPreciseLocation = hasFineLocation
                        isFetchingLocation = true // Сразу запускаем поиск GPS
                    } else {
                        // Разрешения нет (или "Only this time" истекло после закрытия приложения)
                        // Сбрасываем флаги
                        locationPermissionGranted = false
                        isPreciseLocation = false
                        // Запрашиваем у системы
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.size(56.dp) // Увеличиваем размер контейнера, чтобы не обрезать Юпитер
            ) {
                val jupiterComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.jupiter))
                val jupiterProgress by animateLottieCompositionAsState(
                    composition = jupiterComposition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1.5f // Ускоряем Юпитер
                )
                
                Box(contentAlignment = Alignment.Center) {
                    outlineOffsets.forEach { (dx, dy) ->
                        LottieAnimation(
                            composition = jupiterComposition,
                            progress = { jupiterProgress },
                            modifier = Modifier
                                .requiredSize(56.dp)
                                .offset(x = dx, y = dy),
                            dynamicProperties = outlineProperties
                        )
                    }
                    LottieAnimation(
                        composition = jupiterComposition,
                        progress = { jupiterProgress },
                        modifier = Modifier.requiredSize(56.dp) // Увеличиваем размер Юпитера
                    )
                }
            }

            // ПУНКТ 23: Поисковик (State Hoisting) - ПО ЦЕНТРУ
            WeatherSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                focusManager = focusManager,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp) // Увеличиваем отступы, чтобы сузить поисковик
            )

            // Кнопка для перехода в Профиль - СПРАВА (Шестеренка)
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(56.dp) // Размер под стать Юпитеру
            ) {
                val gearComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gear))
                val gearProgress by animateLottieCompositionAsState(
                    composition = gearComposition,
                    iterations = LottieConstants.IterateForever,
                    speed = 0.55f // Слегка уменьшаем скорость анимации шестеренки
                )

                val gearColorFilter = remember {
                    PorterDuffColorFilter(Color(0xFF7E57C2).toArgb(), PorterDuff.Mode.SRC_ATOP)
                }
                val gearDynamicProperties = rememberLottieDynamicProperties(
                    rememberLottieDynamicProperty(
                        property = LottieProperty.COLOR_FILTER,
                        value = gearColorFilter,
                        keyPath = arrayOf("**")
                    )
                )

                Box(contentAlignment = Alignment.Center) {
                    outlineOffsets.forEach { (dx, dy) ->
                        LottieAnimation(
                            composition = gearComposition,
                            progress = { gearProgress },
                            modifier = Modifier
                                .size(40.dp)
                                .offset(x = dx, y = dy),
                            dynamicProperties = outlineProperties
                        )
                    }
                    LottieAnimation(
                        composition = gearComposition,
                        progress = { gearProgress },
                        modifier = Modifier.size(40.dp),
                        dynamicProperties = gearDynamicProperties
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredResults = searchResults.filter { it.id != "my_location_id" }
        // РЕЗУЛЬТАТЫ ПОИСКА (Если ввели текст)
        if (searchQuery.isNotBlank() && filteredResults.isNotEmpty()) {
            Text("Результаты поиска:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredResults, key = { it.id }) { city ->
                    // Анимация цвета при добавлении (Претензия 1)
                    var isAdded by remember { mutableStateOf(false) }
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isAdded) Color(0xFF1E88E5) else MaterialTheme.colorScheme.surfaceVariant,
                        animationSpec = tween(durationMillis = 300)
                    )

                    // Эффект сброса цвета обратно
                    LaunchedEffect(isAdded) {
                        if (isAdded) {
                            delay(300) // Держим синий цвет треть секунды
                            isAdded = false // Возвращаем обычный цвет
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(city.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text(city.country, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                        // Кнопка сохранения города
                        IconButton(onClick = { 
                            // Проверяем, есть ли город уже в сохраненных (uiState.savedCities)
                            val isAlreadySaved = (uiState as? WeatherUiState.Success)?.savedCities?.values?.flatten()?.any { it.id == city.id } == true
                            
                            if (isAlreadySaved) {
                                AppTranslator.showTranslatedToast(context, "Вы уже добавили этот город!")
                            } else {
                                isAdded = true // Запускаем синюю анимацию
                                onToggleCitySave(city.id, true) // Добавляем в БД
                            }
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Добавить город", tint = Color.White)
                        }
                    }
                }
            }
        } 
        // ГЛАВНЫЙ ЭКРАН (Сохраненные города)
        else {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                }
                is WeatherUiState.Error -> {
                    Column(
                        modifier = Modifier.padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetryClick) {
                            Text("Повторить попытку")
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    if (state.savedCities.isEmpty()) {
                        // EMPTY STATE: Городов нет
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Выберите в поиске вашу страну и ваш город!",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // НОВАЯ СТРОЧКА: Сортируем страны. "Текущее место" получает приоритет 0 (самый верх), остальные - 1.
                            val orderedCountries = state.savedCities.keys.sortedBy { if (it == "Текущее место") 0 else 1 }
                            
                            orderedCountries.forEach { country ->
                                val cities = state.savedCities[country]!! // Берем города по отсортированному списку
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
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                                onToggleCitySave(city.id, false)
                                                true
                                            } else false
                                        }
                                    )

                                    // ЕДИНСТВЕННО ВЕРНЫЙ СПОСОБ ОТСЛЕДИТЬ ФИЗИЧЕСКОЕ ДВИЖЕНИЕ СРАЗУ
                                    // requireOffset() показывает реальный сдвиг в пикселях. Если не 0.0f - мы тащим карточку.
                                    // try-catch нужен, потому что при ПЕРВОЙ отрисовке offset еще не существует и кинет ошибку.
                                    val isSwiping = try {
                                        dismissState.requireOffset() != 0f
                                    } catch (e: Exception) {
                                        false
                                    }

                                    // Мгновенное переключение цвета для САМОЙ КАРТОЧКИ
                                    val cardColor = if (isSwiping) Color(0xFFC25953) else MaterialTheme.colorScheme.surfaceVariant

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                            // Фон ПОД карточкой делаем полностью прозрачным
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Transparent)
                                            )
                                        }
                                    ) {
                                        // Передаем цвет ВНУТРЬ карточки
                                        CityCard(city = city, onCityClick = onCityClick, backgroundColor = cardColor)
                                    }
                                }
                            }
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
        label = { Text("Поиск города", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
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

// Функция для маппинга WMO кода в Lottie анимацию
@Composable
fun WeatherIcon(weatherCode: Int, modifier: Modifier = Modifier) {
    val resId = when (weatherCode) {
        0 -> R.raw.sun // Ясно
        1, 2, 3 -> R.raw.clouds // Переменная облачность / Пасмурно
        45, 48 -> R.raw.clouds // Туман (заменили на облака)
        51, 53, 55, 56, 57 -> R.raw.rain // Морось
        61, 63, 65, 66, 67 -> R.raw.rain // Дождь
        71, 73, 75, 77 -> R.raw.snow // Снег
        80, 81, 82 -> R.raw.rain // Ливень
        85, 86 -> R.raw.snow // Снегопад
        95, 96, 99 -> R.raw.thunderstorm // Гроза
        else -> R.raw.clouds // Облачно, переменная облачность, туман и всё остальное
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = if (resId == R.raw.clouds) 1.35f else 1.0f
    )

    val iconSize = when (resId) {
        R.raw.sun -> 58.dp
        R.raw.thunderstorm, R.raw.snow -> 64.dp
        else -> 42.dp
    }

    Box(
        modifier = modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.requiredSize(iconSize)
        )
    }
}

@Composable
fun CityCard(city: CityWeather, onCityClick: (String) -> Unit, backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant) {
    // Состояние для анимации: раскрыта карточка или нет
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // ПУНКТ 21: Проверка TalkBack (объединяем чтение карточки в один блок)
            .semantics(mergeDescendants = true) {
                contentDescription = "Город ({city.name}, Температура){city.temp} градусов"
            }
            // Клик для раскрытия карточки (Оставляем анимацию как было в Требовании 15)
            .clickable { expanded = !expanded }
            // ПУНКТ 22: Анимировать раскрытие карточки
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (city.name == "Приблизительная местность") "Местность" else city.name,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f) // Заставляем текст занимать только свободное место
                        .padding(end = 8.dp), // Даем отступ от градусов
                    maxLines = 1, // Запрещаем перенос на вторую строку
                    overflow = TextOverflow.Ellipsis // Ставим многоточие, если не влезло
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ПРОФЕССИОНАЛЬНЫЙ UX: Показываем лоадер, если данные грузятся
                    if (city.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Показываем статичную иконку погоды перед температурой
                        WeatherIcon(
                            weatherCode = city.weatherCode, 
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    val tempText = if (city.temp > 0) "+${city.temp}°C" else "${city.temp}°C"
                    Text(
                        text = tempText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }
            }

            // Если карточка раскрыта - показываем дополнительную анимационную часть
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Дополнительные данные загружаются...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                val feelsLikeTemp = city.temp - 2
                val feelsLikeText = if (feelsLikeTemp > 0) "+${feelsLikeTemp}°C" else "${feelsLikeTemp}°C"
                Text(
                    text = "Ощущается как: $feelsLikeText",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // ПУНКТ 21: Кнопка навигации на экран деталей
                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                Button(
                    onClick = { onCityClick(city.name) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6650A4),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color.White else Color.Black)
                ) {
                    Text("Подробнее", color = Color.White)
                }
            }
        }
    }
}

// Экран Деталей (Требование 21, Советы по одежде)
@Composable
fun CityDetailsScreen(cityName: String, temp: Int?, weatherCode: Int?, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val options = when {
            weatherCode in listOf(95, 96, 99) -> listOf(
                "На улице ожидается гроза и сильный ветер — лучше отложить долгие прогулки и захватить надёжный дождевик.",
                "Небо хмурится, возможна гроза. Держите зонт под рукой и выбирайте закрытую непромокаемую обувь.",
                "Грозовая погода требует осторожности: оденьтесь практично и спланируйте маршрут ближе к уютным местам."
            )
            weatherCode in listOf(61, 63, 65, 66, 67, 80, 81, 82) -> listOf(
                "За окном дождливо — обязательно возьмите с собой крепкий зонт и наденьте непромокаемую куртку.",
                "Сегодня город умывает дождь. Отличный повод надеть плащ с капюшоном и захватить тёплый напиток в дорогу.",
                "Ожидаются осадки: защитите обувь от влаги и не забудьте закинуть зонтик в сумку перед выходом."
            )
            weatherCode in listOf(51, 53, 55, 56, 57) -> listOf(
                "В воздухе висит лёгкая морось — пригодится ветровка с капюшоном или компактный складной зонт.",
                "Моросящая погода создаёт особую атмосферу, но чтобы не промокнуть, лучше накинуть влагозащитную куртку.",
                "Лёгкие осадки и повышенная влажность: советуем одеться многослойно и захватить зонт на всякий случай."
            )
            weatherCode in listOf(71, 73, 75, 77, 85, 86) -> listOf(
                "Кружатся снежинки и подмораживает — самое время для тёплого пуховика, шарфа и перчаток.",
                "Сегодня снежно: выбирайте зимнюю обувь с нескользящей подошвой и одевайтесь максимально тепло.",
                "Настоящая зимняя погода со снегом — укутайтесь потеплее, не забудьте шапку и берегите руки от холода."
            )
            temp != null && temp <= 0 -> listOf(
                "На улице морозно и свежо: обязательно наденьте зимнюю куртку, тёплую шапку и перчатки.",
                "Температура опустилась ниже нуля — выбирайте термобельё или тёплый свитер, чтобы чувствовать себя комфортно.",
                "Холодный воздух бодрит, но требует утепления: плотная верхняя одежда и шарф сегодня незаменимы."
            )
            temp != null && temp in 1..7 -> listOf(
                "Сегодня довольно холодно и зябко — советуем надеть утеплённую куртку или пальто со свитером.",
                "Прохладный день: чтобы ветер не застал врасплох, выбирайте закрытую одежду и захватите шарф.",
                "За окном бодрящая прохлада. Многослойный образ с тёплой кофтой и курткой подойдёт идеально."
            )
            temp != null && temp in 8..14 -> listOf(
                "Умеренная прохлада: отлично подойдёт демисезонная куртка, тренч или плотная толстовка.",
                "На улице свежо, но вполне комфортно — накиньте ветровку или лёгкое пальто для приятной прогулки.",
                "Погода переменчива: днём может быть уютно в худи, но к вечеру точно пригодится куртка."
            )
            temp != null && temp in 15..21 -> listOf(
                "Мягкая и очень комфортная погода! Достаточно лёгкой кофты, рубашки или джинсовки для долгой прогулки.",
                "Идеальный баланс тепла и свежести — одевайтесь свободно, но захватите лёгкую накидку на вечер.",
                "Замечательная погода за окном: самое время пройтись пешком на свежем воздухе в лёгкой и удобной одежде."
            )
            temp != null && temp in 22..27 -> listOf(
                "На улице по-летнему тепло и приятно! Выбирайте лёгкую дышащую одежду и наслаждайтесь солнечным днём.",
                "Тёплый и ласковый день — футболка и лёгкие брюки или шорты обеспечат максимальный комфорт.",
                "Отличная тёплая погода: тяжёлые вещи можно оставить дома, но не забудьте солнцезащитные очки."
            )
            temp != null && temp >= 28 -> listOf(
                "Сегодня по-настоящему жарко! Одевайтесь максимально легко, выбирайте светлые ткани и пейте больше воды.",
                "Знойная погода: обязательно захватите головной убор, бутылку прохладной воды и держитесь тени в полдень.",
                "Столбик термометра высоко — отдайте предпочтение свободным вещам из хлопка и льна и берегитесь палящего солнца."
            )
            else -> listOf(
                "Приятная погода для прогулок и повседневных дел — одевайтесь по сезону и проведите этот день с комфортом."
            )
        }

        val index = abs(cityName.hashCode() + (temp ?: 0)) % options.size
        val weatherAdvice = options[index]

        Text(
            text = cityName,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = weatherAdvice,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBackClick) {
            Text("Назад")
        }
    }
}

// Экран Профиля/Редактора (Требование 12, 21, 24 и 26)
@Composable
fun ProfileScreen(
    userData: UserData,
    onSaveProfile: (String, String) -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onAvatarChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Локальные состояния
    var name by remember(userData) { mutableStateOf(userData.name) }
    var city by remember(userData) { mutableStateOf(userData.city) }
    var avatarUri by remember(userData.avatarUri) { mutableStateOf(userData.avatarUri) }

    var isNameError by remember { mutableStateOf(false) }
    var isCityError by remember { mutableStateOf(false) }
    
    // Состояние для показа диалога (Изменить / Удалить)
    var showAvatarDialog by remember { mutableStateOf(false) }

    var showExitDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val cityFocusRequester = remember { FocusRequester() }

    // ПУНКТ 26: 2. Лаунчер для выбора картинки из галереи (PhotoPicker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Удаляем старые файлы аватарок
            context.filesDir.listFiles()?.filter { it.name.startsWith("profile_avatar") }?.forEach { it.delete() }
            
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            avatarUri = file.absolutePath
            onAvatarChange(file.absolutePath)
        }
    }

    // ПУНКТ 26: 1. Лаунчер для запроса разрешения на чтение галереи
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение дали -> запускаем фотопикер
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // Отказали -> обрабатываем отказ, показываем Toast
            AppTranslator.showTranslatedToast(context, "Для выбора аватарки нужен доступ к галерее")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Шаг 1: Аватарка по центру с крупными анимациями чуть ниже по бокам
        val moonComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.moon))
        val moonProgress by animateLottieCompositionAsState(
            composition = moonComposition,
            iterations = LottieConstants.IterateForever,
            speed = 1.2f // Ускоряем анимацию в 2.5 раза
        )

        val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val outlineColor = if (isDarkTheme) Color.White else Color.Black
        val outlineColorFilter = remember(outlineColor) {
            PorterDuffColorFilter(outlineColor.toArgb(), PorterDuff.Mode.SRC_ATOP)
        }
        val outlineProperties = rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR_FILTER,
                value = outlineColorFilter,
                keyPath = arrayOf("**")
            )
        )
        val outlineOffsets = remember {
            listOf(
                Pair(-0.8.dp, 0.dp),
                Pair(0.8.dp, 0.dp),
                Pair(0.dp, -0.8.dp),
                Pair(0.dp, 0.8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Левая анимация (опущена ниже и увеличена за счет обхода прозрачных полей)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = 42.dp), // Опускаем ниже к краю аватарки как на макете
                contentAlignment = Alignment.Center
            ) {
                outlineOffsets.forEach { (dx, dy) ->
                    LottieAnimation(
                        composition = moonComposition,
                        progress = { moonProgress },
                        modifier = Modifier
                            .requiredSize(210.dp)
                            .offset(x = dx, y = dy),
                        dynamicProperties = outlineProperties
                    )
                }
                LottieAnimation(
                    composition = moonComposition,
                    progress = { moonProgress },
                    modifier = Modifier.requiredSize(210.dp) // Увеличиваем саму капсулу в 5 раз
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Круглая Аватарка
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
                    .clickable { showAvatarDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri.isBlank() || avatarUri.startsWith("content://")) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Аватарка по умолчанию",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Gray
                    )
                } else {
                    AsyncImage(
                        model = File(avatarUri),
                        contentDescription = "Аватарка профиля",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Правая анимация (опущена ниже и увеличена за счет обхода прозрачных полей)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = 42.dp), // Опускаем ниже к краю аватарки как на макете
                contentAlignment = Alignment.Center
            ) {
                outlineOffsets.forEach { (dx, dy) ->
                    LottieAnimation(
                        composition = moonComposition,
                        progress = { moonProgress },
                        modifier = Modifier
                            .requiredSize(210.dp)
                            .offset(x = dx, y = dy),
                        dynamicProperties = outlineProperties
                    )
                }
                LottieAnimation(
                    composition = moonComposition,
                    progress = { moonProgress },
                    modifier = Modifier.requiredSize(210.dp) // Увеличиваем саму капсулу в 5 раз
                )
            }
        }

        // Диалоговое окно для работы с аватаркой (БЕЗ ИЗМЕНЕНИЙ)
        if (showAvatarDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarDialog = false },
                title = { Text("Фото профиля") },
                text = { Text("Выберите действие с аватаркой") },
                confirmButton = {
                    TextButton(onClick = {
                        showAvatarDialog = false
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        permissionLauncher.launch(permission)
                    }) {
                        Text("Изменить")
                    }
                },
                dismissButton = {
                    if (avatarUri.isNotBlank()) {
                        TextButton(onClick = {
                            showAvatarDialog = false
                            avatarUri = ""
                            onAvatarChange("") 
                        }) {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        TextButton(onClick = { showAvatarDialog = false }) {
                            Text("Отмена")
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Поле 1: Страна (Шаг 2)
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                isNameError = it.isBlank()
            },
            label = { Text("Страна") }, // Переименовали Label
            isError = isNameError,
            supportingText = { if (isNameError) Text("Страна не может быть пустой") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { cityFocusRequester.requestFocus() }
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле 2: Город
        OutlinedTextField(
            value = city,
            onValueChange = { 
                city = it
                isCityError = it.isBlank()
            },
            label = { Text("Любимый город") },
            isError = isCityError,
            supportingText = { if (isCityError) Text("Город не может быть пустым") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(force = true) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(cityFocusRequester),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Кнопка сохранения с валидацией
        Button(
            onClick = {
                isNameError = name.isBlank()
                isCityError = city.isBlank()
                
                if (!isNameError && !isCityError) {
                    focusManager.clearFocus()
                    onSaveProfile(name, city)
                    AppTranslator.showTranslatedToast(context, "Настройки сохранены!")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить настройки")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Шаг 3: Новая кнопка навигации "вернуться назад"
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Вернуться назад", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LanguageSelectorButton(containerColor = Color(0xFFB548C6))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Тёмная тема", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = userData.isDarkTheme,
                onCheckedChange = { onThemeChange(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { showExitDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из приложения", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Выход", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = { Text("Вы точно хотите выйти?", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Нет", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(48.dp))
                    TextButton(onClick = { 
                        showExitDialog = false
                        (context as? Activity)?.finishAndRemoveTask() 
                    }) {
                        Text("Да", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}
