package com.example.weatherapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Лениво создаем DataStore (это официальный подход от Google)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

// Наша модель данных пользователя
data class UserData(val name: String, val city: String, val isDarkTheme: Boolean, val avatarUri: String)

interface IUserPreferencesRepository {
    val userDataFlow: Flow<UserData>
    suspend fun saveUserData(name: String, city: String)
    suspend fun setDarkTheme(isDarkTheme: Boolean)
    suspend fun saveAvatarUri(uri: String)
}

open class UserPreferencesRepository(private val context: Context) : IUserPreferencesRepository {
    
    // Ключи, по которым мы будем сохранять и доставать данные
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val FAVORITE_CITY = stringPreferencesKey("favorite_city")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val AVATAR_URI = stringPreferencesKey("avatar_uri") // ПУНКТ 26: Сохраняем путь к картинке
    }

    // Реактивный Flow, который всегда отдаст самые свежие данные из хранилища
    override val userDataFlow: Flow<UserData> = context.dataStore.data.map { preferences ->
        UserData(
            name = preferences[PreferencesKeys.USER_NAME] ?: "",
            city = preferences[PreferencesKeys.FAVORITE_CITY] ?: "",
            isDarkTheme = preferences[PreferencesKeys.IS_DARK_THEME] ?: false,
            avatarUri = preferences[PreferencesKeys.AVATAR_URI] ?: ""
        )
    }

    // Функция сохранения Имени и Города (для Требования 24)
    override suspend fun saveUserData(name: String, city: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.FAVORITE_CITY] = city
        }
    }
    
    // Функция для будущей Темной Темы (для Требования 13)
    override suspend fun setDarkTheme(isDarkTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDarkTheme
        }
    }

    // Функция сохранения Аватарки (для Требования 26)
    override suspend fun saveAvatarUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AVATAR_URI] = uri
        }
    }
}
