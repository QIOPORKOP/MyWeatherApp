# WeatherApp 🌤️

## Описание проекта (Итоговое приложение)
WeatherApp — это современное Android-приложение для получения прогноза погоды в реальном времени.

### Основной функционал:
* Прогноз погоды для 38 городов мира и по точной/приблизительной GPS-геолокации.
* 100% офлайн-перевод интерфейса на 14 языков (система `AppTranslator`).
* Умные советы по выбору одежды в зависимости от текущей температуры и погодных условий.
* Заставка, экраны Политики конфиденциальности и Пользовательского соглашения.
* Профиль пользователя с возможностью загрузки фото, выбора страны, города и переключением тёмной/светлой темы.

---

## Архитектура приложения
Проект построен по стандарту **Google Guide to App Architecture**, с четким разделением на слои и использованием современных инструментов Android разработки.

### UI Layer (Jetpack Compose + Material 3)
* **Экраны**: `MainActivity`, `WeatherMainScreen`, `CityDetailsScreen`, `ProfileScreen`, `WelcomeScreen`, `LegalFlowScreen`.
* **Паттерн**: Однонаправленный поток данных (UDF - Unidirectional Data Flow).
* **Состояние**: Сбор состояния UI через `collectAsStateWithLifecycle`.
* **Навигация**: Navigation Compose с поддержкой Deep Links.
* **Анимации**: Lottie.

### Domain / State Holder Layer
* **ViewModel**: `WeatherViewModel` управляет состоянием UI (`StateFlow`).
* **Логика**: Обработка поисковых запросов с задержкой (`debounce`).
* **Сохранение состояния**: Использование `SavedStateHandle` для переживания смерти процесса.

### Data Layer (Repository Pattern)
* Реализован контракт `WeatherRepository` через `RealWeatherRepository` и `FakeWeatherRepository` (для тестов).

#### Сеть (API)
* **Библиотеки**: Retrofit2 + Gson.
* **Источник данных**: Open-Meteo API.
* **Особенности**: Пакетная загрузка погоды (batch) для оптимизации сети и программный 2-часовой кэш.

#### Локальная База Данных (Room)
* **Компоненты**: `AppDatabase`, `WeatherDao`, `CityWeatherEntity`.
* **Особенности**: Реактивное чтение данных через `Flow`, поддержка CRUD операций и работа в офлайн-режиме (отображение закэшированных данных).

#### Хранилище настроек (Jetpack DataStore & SharedPreferences)
* **Репозиторий**: `UserPreferencesRepository`.
* **Сохраняемые данные**: Выбранная тема оформления, страна, любимый город, URI аватарки профиля и статус принятия юридических документов (`is_legal_accepted`).

---

## Тестирование
* **Unit-тесты**: Написаны тесты для логики ViewModel и сопрограмм (корутин) с использованием `MockK` и `kotlinx-coroutines-test`.
* **UI-тесты**: Реализованы тесты для проверки компонентов интерфейса, написанных на Jetpack Compose.

---

## Поставка — CI/CD Pipeline и сборка релиза (AAB)

В проекте настроен процесс непрерывной интеграции (CI) с помощью GitHub Actions.

### Описание этапов пайплайна (`.github/workflows/android-ci.yml`):
1. **Checkout**: Загрузка исходного кода репозитория.
2. **Setup JDK**: Настройка Java Development Kit (версия 21).
3. **Lint**: Проверка качества кода с помощью Android Lint (`./gradlew lintDebug`).
4. **Unit Tests**: Запуск модульных тестов (`./gradlew testDebugUnitTest`).
5. **Signed Release AAB Build**: Сборка подписанного Android App Bundle для релиза (`./gradlew bundleRelease`).
6. **Artifact Upload**: Загрузка готового файла `app-release.aab` в артефакты GitHub Actions.

### Локальная сборка релиза:
Проект уже содержит сгенерированный ключ (`release-key.jks`) и настроенный блок `signingConfigs` в `app/build.gradle.kts`.

Для ручной сборки подписанного AAB выполните команду в терминале:
```bash
./gradlew bundleRelease
```

Путь к готовому файлу:
`app/build/outputs/bundle/release/app-release.aab`

Разработчик: Жолманов Еркебұлан (QIOPORKOP) © 2026. Все права защищены.
