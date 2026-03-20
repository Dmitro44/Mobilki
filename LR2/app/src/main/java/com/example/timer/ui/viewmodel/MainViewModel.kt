package com.example.timer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.domain.model.TimerSequenceModel
import com.example.timer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: TimerRepository,
    private val preferencesRepository: com.example.timer.domain.repository.PreferencesRepository
) : ViewModel() {
    
    val sequences: StateFlow<List<TimerSequenceModel>> = repository
        .getAllSequences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val userPreferences = preferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.timer.data.local.preferences.UserPreferences()
        )
    
    fun deleteSequence(sequence: TimerSequenceModel) {
        viewModelScope.launch {
            repository.deleteSequence(sequence)
        }
    }
    
    fun deleteSequenceById(sequenceId: Long) {
        viewModelScope.launch {
            repository.deleteSequenceById(sequenceId)
        }
    }
    
    fun deleteAllSequences() {
        viewModelScope.launch {
            repository.deleteAllSequences()
        }
    }
}
