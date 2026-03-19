package com.example.timer.domain.repository

import com.example.timer.data.local.preferences.FontSize
import com.example.timer.data.local.preferences.Language
import com.example.timer.data.local.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences operations
 */
interface PreferencesRepository {
    
    /**
     * Flow of user preferences
     * Emits whenever preferences change
     */
    val userPreferences: Flow<UserPreferences>
    
    /**
     * Update dark theme setting
     */
    suspend fun setDarkTheme(isDark: Boolean)
    
    /**
     * Update font size setting
     */
    suspend fun setFontSize(fontSize: FontSize)
    
    /**
     * Update language setting
     */
    suspend fun setLanguage(language: Language)
    
    /**
     * Clear all preferences (reset to defaults)
     */
    suspend fun clearPreferences()
}
