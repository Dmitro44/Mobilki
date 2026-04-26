package com.example.seabattle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.GameRepository
import com.example.seabattle.model.GameSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val games: List<GameSummary> = emptyList(),
    val errorMessage: String? = null,
) {
    val totalGames: Int
        get() = games.size
    val wins: Int
        get() = games.count { it.didWin }
    val losses: Int
        get() = games.count { !it.didWin }
}

class HistoryViewModel(
    private val repository: GameRepository?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun observeHistory(uid: String) {
        val repo = repository ?: return
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repo.observeHistory(uid).collectLatest { history ->
                    _uiState.value = HistoryUiState(isLoading = false, games = history)
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load history",
                )
            }
        }
    }
}
