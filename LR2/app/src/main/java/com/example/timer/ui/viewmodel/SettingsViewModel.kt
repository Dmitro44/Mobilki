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

class SettingsViewModel(
    private val repository: PreferencesRepository,
    private val timerRepository: com.example.timer.domain.repository.TimerRepository
) : ViewModel() {
    
    val sequences = timerRepository.getAllSequences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val userPreferences: StateFlow<UserPreferences> = repository
        .userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )
    
    fun deleteAllSequences() {
        viewModelScope.launch {
            timerRepository.deleteAllSequences()
        }
    }
    
    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(isDark)
        }
    }
    
    fun setFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            repository.setFontSize(fontSize)
        }
    }
    
    fun setLanguage(context: Context, language: Language, onLanguageApplied: () -> Unit) {
        viewModelScope.launch {
            repository.setLanguage(language)
            // Apply language immediately
            LocaleHelper.setLocale(context, language)
            onLanguageApplied()
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearPreferences()
        }
    }
}
