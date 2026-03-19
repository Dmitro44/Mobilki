package com.example.timer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.domain.model.TimerSequenceModel
import com.example.timer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Main screen
 * 
 * Responsibilities:
 * - Expose list of timer sequences as StateFlow
 * - Handle sequence deletion
 * - Manage loading and error states
 * 
 * The sequences Flow is converted to StateFlow for UI observation
 * with WhileSubscribed timeout to stop collection when UI is not visible
 */
class MainViewModel(
    private val repository: TimerRepository
) : ViewModel() {
    
    /**
     * StateFlow of all timer sequences
     * Automatically updates when database changes
     */
    val sequences: StateFlow<List<TimerSequenceModel>> = repository
        .getAllSequences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Delete a timer sequence
     * @param sequence The sequence to delete
     */
    fun deleteSequence(sequence: TimerSequenceModel) {
        viewModelScope.launch {
            repository.deleteSequence(sequence)
        }
    }
    
    /**
     * Delete a timer sequence by ID
     * @param sequenceId The ID of the sequence to delete
     */
    fun deleteSequenceById(sequenceId: Long) {
        viewModelScope.launch {
            repository.deleteSequenceById(sequenceId)
        }
    }
    
    /**
     * Delete all timer sequences
     * Use with caution - typically for testing or reset functionality
     */
    fun deleteAllSequences() {
        viewModelScope.launch {
            repository.deleteAllSequences()
        }
    }
}
