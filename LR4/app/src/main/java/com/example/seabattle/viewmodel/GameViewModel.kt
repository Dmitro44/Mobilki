package com.example.seabattle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.GameRepository
import com.example.seabattle.game.BoardGenerator
import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus
import com.example.seabattle.model.Profile
import com.example.seabattle.model.Ship
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val gameId: String = "",
    val localShips: List<Ship> = BoardGenerator.generateFleet(),
    val currentGame: GameState? = null,
    val errorMessage: String? = null,
    val isSubmittingGuestShot: Boolean = false,
) {
    val hasJoinedGame: Boolean
        get() = gameId.isNotBlank() && currentGame != null
}

class GameViewModel(
    private val repository: GameRepository?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameObservationJob: Job? = null

    fun rerollFleet() {
        val game = _uiState.value.currentGame
        if (game?.hostReady == true || game?.guestReady == true) {
            return
        }
        _uiState.value = _uiState.value.copy(localShips = BoardGenerator.generateFleet())
    }

    fun createGame(profile: Profile) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repo.createGame(profile.uid, profile)
            }.onSuccess { gameId ->
                _uiState.value = _uiState.value.copy(isLoading = false, gameId = gameId)
                observeGame(gameId, profile.uid)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to create game",
                )
            }
        }
    }

    fun joinGame(gameId: String, profile: Profile) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repo.joinGame(gameId.trim().uppercase(), profile.uid, profile)
                gameId.trim().uppercase()
            }.onSuccess { normalizedId ->
                _uiState.value = _uiState.value.copy(isLoading = false, gameId = normalizedId)
                observeGame(normalizedId, profile.uid)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to join game",
                )
            }
        }
    }

    fun markReady(currentUserId: String) {
        val repo = repository ?: return
        val gameId = _uiState.value.gameId
        if (gameId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repo.markReady(gameId, currentUserId, _uiState.value.localShips)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to mark ready",
                )
            }
        }
    }

    fun hostFire(currentUserId: String, cellIndex: Int) {
        val repo = repository ?: return
        val game = _uiState.value.currentGame ?: return
        if (game.status != GameStatus.HOST_TURN || game.hostUid != currentUserId) return

        viewModelScope.launch {
            runCatching {
                repo.hostFire(game.gameId, currentUserId, cellIndex)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error.message ?: "Failed to fire")
            }
        }
    }

    fun guestFire(currentUserId: String, cellIndex: Int) {
        val repo = repository ?: return
        val game = _uiState.value.currentGame ?: return
        if (
            game.status != GameStatus.GUEST_TURN ||
            game.guestUid != currentUserId ||
            game.pendingGuestShot != null ||
            _uiState.value.isSubmittingGuestShot
        ) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingGuestShot = true, errorMessage = null)
            runCatching {
                repo.submitGuestShot(game.gameId, currentUserId, cellIndex)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingGuestShot = false,
                    errorMessage = error.message ?: "Failed to submit shot"
                )
            }
        }
    }

    private fun observeGame(gameId: String, currentUserId: String) {
        val repo = repository ?: return
        gameObservationJob?.cancel()
        gameObservationJob = viewModelScope.launch {
            repo.observeGame(gameId).collectLatest { game ->
                _uiState.value = _uiState.value.copy(
                    currentGame = game,
                    isSubmittingGuestShot = game?.pendingGuestShot?.shooterUid == currentUserId,
                )

                if (
                    game != null &&
                    game.hostUid == currentUserId &&
                    game.status == GameStatus.GUEST_TURN &&
                    game.pendingGuestShot != null &&
                    game.pendingGuestShot.requestId != game.lastProcessedGuestRequestId
                ) {
                    runCatching {
                        repo.processPendingGuestShot(gameId, currentUserId)
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message ?: "Failed to process guest shot",
                        )
                    }
                }
            }
        }
    }

    fun leaveGame(currentUserId: String) {
        val repo = repository
        val game = _uiState.value.currentGame
        viewModelScope.launch {
            runCatching {
                if (repo != null && game != null) {
                    repo.leaveGame(game.gameId, currentUserId)
                }
            }
        }
        gameObservationJob?.cancel()
        _uiState.value = GameUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
