package com.example.timer.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class PreferencesManager private constructor(private val context: Context) {
    
    private val dataStore: DataStore<Preferences> = context.dataStore
    
    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_FONT_SIZE = intPreferencesKey("font_size")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            isDarkTheme = preferences[KEY_DARK_THEME] ?: false,
            fontSize = FontSize.fromOrdinal(preferences[KEY_FONT_SIZE] ?: FontSize.MEDIUM.ordinal),
            language = Language.fromCode(preferences[KEY_LANGUAGE] ?: Language.ENGLISH.code)
        )
    }
    
    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = isDark
        }
    }
    
    suspend fun setFontSize(fontSize: FontSize) {
        dataStore.edit { preferences ->
            preferences[KEY_FONT_SIZE] = fontSize.ordinal
        }
    }
    
    suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language.code
        }
    }
}
