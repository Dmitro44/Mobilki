package com.example.timer.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhaseModel
import com.example.timer.domain.model.TimerSequenceModel
import com.example.timer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditViewModel(
    private val repository: TimerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()
    
    private var editingSequenceId: Long? = null
    
    fun loadSequence(sequenceId: Long?) {
        if (sequenceId == null || sequenceId == 0L) {
            _uiState.value = EditUiState()
            editingSequenceId = null
        } else {
            editingSequenceId = sequenceId
            viewModelScope.launch {
                val sequence = repository.getSequenceById(sequenceId)
                if (sequence != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            name = sequence.name,
                            selectedColor = sequence.color,
                            phases = sequence.phases.toMutableList(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Sequence not found") }
                }
            }
        }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }
    
    fun updateColor(color: Color) {
        _uiState.update { it.copy(selectedColor = color) }
    }
    
    fun addPhase(
        phaseType: PhaseType = PhaseType.WORK,
        durationSeconds: Int = 60,
        repetitions: Int = 1
    ) {
        _uiState.update { currentState ->
            val newPhase = TimerPhaseModel(
                id = 0,
                sequenceId = editingSequenceId ?: 0,
                phaseType = phaseType,
                durationSeconds = durationSeconds,
                repetitions = repetitions,
                order = currentState.phases.size
            )
            currentState.copy(phases = (currentState.phases + newPhase).toMutableList())
        }
    }
    
    fun updatePhase(index: Int, updatedPhase: TimerPhaseModel) {
        _uiState.update { currentState ->
            val phases = currentState.phases.toMutableList()
            if (index in phases.indices) {
                phases[index] = updatedPhase.copy(order = index)
            }
            currentState.copy(phases = phases)
        }
    }
    
    fun removePhase(index: Int) {
        _uiState.update { currentState ->
            val phases = currentState.phases.toMutableList()
            if (index in phases.indices) {
                phases.removeAt(index)
                // Reorder remaining phases
                phases.forEachIndexed { newIndex, phase ->
                    phases[newIndex] = phase.copy(order = newIndex)
                }
            }
            currentState.copy(phases = phases)
        }
    }
    
    fun movePhaseUp(index: Int) {
        if (index <= 0) return
        _uiState.update { currentState ->
            val phases = currentState.phases.toMutableList()
            val temp = phases[index]
            phases[index] = phases[index - 1].copy(order = index)
            phases[index - 1] = temp.copy(order = index - 1)
            currentState.copy(phases = phases)
        }
    }
    
    fun movePhaseDown(index: Int) {
        _uiState.update { currentState ->
            if (index >= currentState.phases.size - 1) return
            val phases = currentState.phases.toMutableList()
            val temp = phases[index]
            phases[index] = phases[index + 1].copy(order = index)
            phases[index + 1] = temp.copy(order = index + 1)
            currentState.copy(phases = phases)
        }
    }
    
    suspend fun saveSequence(): Boolean {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(error = "Sequence name cannot be empty") }
            return false
        }
        
        if (currentState.phases.isEmpty()) {
            _uiState.update { it.copy(error = "Add at least one phase") }
            return false
        }
        
        if (currentState.phases.any { !it.isValid() }) {
            _uiState.update { it.copy(error = "All phases must have valid duration and repetitions") }
            return false
        }
        
        _uiState.update { it.copy(isSaving = true, error = null) }
        
        return try {
            val sequence = TimerSequenceModel(
                id = editingSequenceId ?: 0,
                name = currentState.name,
                color = currentState.selectedColor,
                phases = currentState.phases
            )
            
            if (editingSequenceId != null) {
                repository.updateSequence(sequence)
            } else {
                repository.insertSequence(sequence)
            }
            
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            true
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isSaving = false, 
                    error = "Failed to save: ${e.message}"
                ) 
            }
            false
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class EditUiState(
    val name: String = "",
    val selectedColor: Color = Color(0xFF6200EE), // Default purple
    val phases: List<TimerPhaseModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)
