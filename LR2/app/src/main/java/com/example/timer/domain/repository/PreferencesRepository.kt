package com.example.timer.domain.repository

import com.example.timer.data.local.preferences.FontSize
import com.example.timer.data.local.preferences.Language
import com.example.timer.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    
    val userPreferences: Flow<UserPreferences>
    
    suspend fun setDarkTheme(isDark: Boolean)
    
    suspend fun setFontSize(fontSize: FontSize)
    
    suspend fun setLanguage(language: Language)
    
    suspend fun clearPreferences()
}
