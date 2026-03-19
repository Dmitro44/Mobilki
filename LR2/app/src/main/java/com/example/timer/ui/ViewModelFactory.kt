package com.example.timer.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.timer.data.local.database.AppDatabase
import com.example.timer.data.local.preferences.PreferencesManager
import com.example.timer.data.repository.PreferencesRepositoryImpl
import com.example.timer.data.repository.TimerRepositoryImpl
import com.example.timer.domain.repository.PreferencesRepository
import com.example.timer.domain.repository.TimerRepository
import com.example.timer.ui.viewmodel.EditViewModel
import com.example.timer.ui.viewmodel.MainViewModel
import com.example.timer.ui.viewmodel.SettingsViewModel
import com.example.timer.ui.viewmodel.TimerViewModel

/**
 * ViewModelProvider.Factory for manual dependency injection
 * 
 * Responsibilities:
 * - Creates ViewModels with required dependencies
 * - Manages repository instantiation using AppDatabase singleton
 * - Provides type-safe ViewModel creation
 * 
 * Usage:
 * ```
 * val factory = ViewModelFactory(context)
 * val viewModel: MainViewModel by viewModels { factory }
 * ```
 */
class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    // Lazy initialization of repositories
    private val timerRepository: TimerRepository by lazy {
        val database = AppDatabase.getInstance(context)
        TimerRepositoryImpl(
            sequenceDao = database.timerSequenceDao(),
            phaseDao = database.timerPhaseDao()
        )
    }
    
    private val preferencesRepository: PreferencesRepository by lazy {
        val preferencesManager = PreferencesManager.getInstance(context)
        PreferencesRepositoryImpl(preferencesManager)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(timerRepository) as T
            }
            modelClass.isAssignableFrom(TimerViewModel::class.java) -> {
                TimerViewModel() as T
            }
            modelClass.isAssignableFrom(EditViewModel::class.java) -> {
                EditViewModel(timerRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(preferencesRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
