package com.example.timer.data.repository

import com.example.timer.data.local.preferences.FontSize
import com.example.timer.data.local.preferences.Language
import com.example.timer.data.local.preferences.PreferencesManager
import com.example.timer.data.local.preferences.UserPreferences
import com.example.timer.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : PreferencesRepository {
    
    override val userPreferences: Flow<UserPreferences>
        get() = preferencesManager.userPreferencesFlow
    
    override suspend fun setDarkTheme(isDark: Boolean) {
        preferencesManager.setDarkTheme(isDark)
    }
    
    override suspend fun setFontSize(fontSize: FontSize) {
        preferencesManager.setFontSize(fontSize)
    }
    
    override suspend fun setLanguage(language: Language) {
        preferencesManager.setLanguage(language)
    }
    
    override suspend fun clearPreferences() {
        preferencesManager.clearPreferences()
    }
}
