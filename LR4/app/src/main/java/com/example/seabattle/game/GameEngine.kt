package com.example.seabattle.game

import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus

object GameEngine {

    fun applyHostShot(state: GameState, cellIndex: Int): GameState {
        require(state.status == GameStatus.HOST_TURN) { "Host turn expected" }
        require(cellIndex !in state.hostShotsMade) { "Cell already targeted" }

        val updatedShotsMade = state.hostShotsMade + cellIndex
        val updatedGuestShotsReceived = state.guestShotsReceived + cellIndex
        val hostWon = allShipCells(state.guestShips).all(updatedGuestShotsReceived::contains)

        return state.copy(
            hostShotsMade = updatedShotsMade,
            guestShotsReceived = updatedGuestShotsReceived,
            currentTurnUid = if (hostWon) state.hostUid else state.guestUid.orEmpty(),
            status = if (hostWon) GameStatus.FINISHED else GameStatus.GUEST_TURN,
            winnerUid = if (hostWon) state.hostUid else null,
            updatedAt = System.currentTimeMillis(),
        )
    }

    fun applyGuestShot(state: GameState, cellIndex: Int): GameState {
        require(state.status == GameStatus.GUEST_TURN) { "Guest turn expected" }
        require(cellIndex !in state.guestShotsMade) { "Cell already targeted" }

        val updatedShotsMade = state.guestShotsMade + cellIndex
        val updatedHostShotsReceived = state.hostShotsReceived + cellIndex
        val guestWon = allShipCells(state.hostShips).all(updatedHostShotsReceived::contains)

        return state.copy(
            guestShotsMade = updatedShotsMade,
            hostShotsReceived = updatedHostShotsReceived,
            currentTurnUid = if (guestWon) state.guestUid.orEmpty() else state.hostUid,
            status = if (guestWon) GameStatus.FINISHED else GameStatus.HOST_TURN,
            winnerUid = if (guestWon) state.guestUid else null,
            updatedAt = System.currentTimeMillis(),
        )
    }

    fun isHit(ships: List<com.example.seabattle.model.Ship>, cellIndex: Int): Boolean {
        return ships.any { cellIndex in it.cells }
    }

    fun allShipCells(ships: List<com.example.seabattle.model.Ship>): Set<Int> {
        return ships.flatMap { it.cells }.toSet()
    }
}
