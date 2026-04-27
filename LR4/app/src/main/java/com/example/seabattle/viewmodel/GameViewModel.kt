package com.example.seabattle.viewmodel

import com.example.seabattle.BoardPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.GameRepository
import com.example.seabattle.game.FleetRules
import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus
import com.example.seabattle.model.Profile
import com.example.seabattle.model.Ship
import com.example.seabattle.model.ShipOrientation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val gameId: String = "",
    val localPlayerUid: String? = null,
    val localShips: List<Ship> = emptyList(),
    val selectedShipSize: Int? = FleetRules.REQUIRED_SHIP_SIZES.maxOrNull(),
    val placementOrientation: ShipOrientation = ShipOrientation.HORIZONTAL,
    val placementErrorMessage: String? = null,
    val currentGame: GameState? = null,
    val errorMessage: String? = null,
    val isSubmittingGuestShot: Boolean = false,
    val selectedEnemyCell: BoardPosition? = null,
) {
    val hasJoinedGame: Boolean
        get() = gameId.isNotBlank() && currentGame != null

    val remainingShipSizes: List<Int>
        get() = FleetRules.remainingShipSizes(localShips)

    val isFleetReady: Boolean
        get() = FleetRules.isValidFleet(localShips)
}

class GameViewModel(
    private val repository: GameRepository?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameObservationJob: Job? = null

    fun selectShipSize(size: Int) {
        if (isPlacementLocked()) return
        if (size in _uiState.value.remainingShipSizes) {
            _uiState.value = _uiState.value.copy(selectedShipSize = size, placementErrorMessage = null)
        }
    }

    fun togglePlacementOrientation() {
        if (isPlacementLocked()) return
        _uiState.value = _uiState.value.copy(
            placementOrientation = if (_uiState.value.placementOrientation == ShipOrientation.HORIZONTAL) {
                ShipOrientation.VERTICAL
            } else {
                ShipOrientation.HORIZONTAL
            },
            placementErrorMessage = null,
        )
    }

    fun clearPlacement() {
        if (isPlacementLocked()) return
        _uiState.value = _uiState.value.copy(
            localShips = emptyList(),
            selectedShipSize = FleetRules.REQUIRED_SHIP_SIZES.maxOrNull(),
            placementErrorMessage = null,
        )
    }

    fun placeOrRemoveShipAt(cellIndex: Int) {
        val currentState = _uiState.value
        if (isPlacementLocked()) return

        val existingShip = FleetRules.findShipAt(currentState.localShips, cellIndex)
        if (existingShip != null) {
            val updatedShips = currentState.localShips - existingShip
            _uiState.value = currentState.copy(
                localShips = updatedShips,
                selectedShipSize = currentState.selectedShipSize ?: existingShip.size,
                placementErrorMessage = null,
            )
            return
        }

        val shipSize = currentState.selectedShipSize ?: return
        val candidate = FleetRules.buildShip(cellIndex, shipSize, currentState.placementOrientation)
        if (candidate == null || !FleetRules.canPlaceShip(currentState.localShips, candidate)) {
            _uiState.value = currentState.copy(
                placementErrorMessage = "You cannot place a ship here.",
            )
            return
        }

        val updatedShips = currentState.localShips + candidate
        val nextSize = FleetRules.remainingShipSizes(updatedShips).firstOrNull()
        _uiState.value = currentState.copy(
            localShips = updatedShips,
            selectedShipSize = nextSize,
            placementErrorMessage = null,
        )
    }

    fun createGame(profile: Profile) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repo.createGame(profile.uid, profile)
            }.onSuccess { gameId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    gameId = gameId,
                    localPlayerUid = profile.uid,
                )
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    gameId = normalizedId,
                    localPlayerUid = profile.uid,
                )
                observeGame(normalizedId, profile.uid)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to join game",
                )
            }
        }
    }

    fun setReady(currentUserId: String, ready: Boolean) {
        val repo = repository ?: return
        val gameId = _uiState.value.gameId
        if (gameId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repo.setReady(gameId, currentUserId, _uiState.value.localShips, ready)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, placementErrorMessage = null)
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
            _uiState.value = _uiState.value.copy(
                selectedEnemyCell = BoardPosition(
                    row = cellIndex / FleetRules.BOARD_SIZE,
                    column = cellIndex % FleetRules.BOARD_SIZE,
                ),
                errorMessage = null,
            )
            runCatching {
                repo.hostFire(game.gameId, currentUserId, cellIndex)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    selectedEnemyCell = null,
                    errorMessage = error.message ?: "Failed to fire"
                )
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
            _uiState.value = _uiState.value.copy(
                isSubmittingGuestShot = true,
                errorMessage = null,
                selectedEnemyCell = BoardPosition(
                    row = cellIndex / FleetRules.BOARD_SIZE,
                    column = cellIndex % FleetRules.BOARD_SIZE,
                ),
            )
            runCatching {
                repo.submitGuestShot(game.gameId, currentUserId, cellIndex)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingGuestShot = false,
                    selectedEnemyCell = null,
                    errorMessage = error.message ?: "Failed to submit shot"
                )
            }
        }
    }

    private fun observeGame(gameId: String, currentUserId: String) {
        val repo = repository ?: return
        gameObservationJob?.cancel()
        gameObservationJob = viewModelScope.launch {
            try {
                repo.observeGame(gameId).collectLatest { game ->
                    _uiState.value = _uiState.value.copy(
                        currentGame = game,
                        isSubmittingGuestShot = game?.pendingGuestShot?.shooterUid == currentUserId,
                        selectedEnemyCell = null,
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
            } catch (_: CancellationException) {
                // Normal local cancellation when leaving battle or resetting state.
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to observe game",
                )
            }
        }
    }

    fun clearLocalGame() {
        gameObservationJob?.cancel()
        _uiState.value = GameUiState()
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
        clearLocalGame()
    }

    private fun isPlacementLocked(): Boolean {
        val state = _uiState.value
        val game = state.currentGame ?: return false
        val localUid = state.localPlayerUid ?: return false
        return if (localUid == game.hostUid) {
            game.hostReady
        } else {
            game.guestReady
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
