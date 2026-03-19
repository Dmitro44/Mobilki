package com.example.timer.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.data.local.preferences.FontSize
import com.example.timer.data.local.preferences.Language
import com.example.timer.data.local.preferences.UserPreferences
import com.example.timer.domain.repository.PreferencesRepository
import com.example.timer.ui.util.LocaleHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen
 * 
 * Responsibilities:
 * - Expose user preferences as StateFlow
 * - Handle preference updates (theme, font size, language)
 * - Persist changes through PreferencesRepository
 * 
 * @param repository Repository for preference operations
 */
class SettingsViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {
    
    /**
     * StateFlow of user preferences
     * Automatically updates when preferences change
     */
    val userPreferences: StateFlow<UserPreferences> = repository
        .userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )
    
    /**
     * Toggle dark theme on/off
     */
    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(isDark)
        }
    }
    
    /**
     * Update font size preference
     */
    fun setFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            repository.setFontSize(fontSize)
        }
    }
    
    /**
     * Update language preference
     */
    fun setLanguage(context: Context, language: Language, onLanguageApplied: () -> Unit) {
        viewModelScope.launch {
            repository.setLanguage(language)
            // Apply language immediately
            LocaleHelper.setLocale(context, language)
            onLanguageApplied()
        }
    }
    
    /**
     * Reset all preferences to defaults
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearPreferences()
        }
    }
}
