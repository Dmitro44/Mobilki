package com.example.seabattle.game

import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus

object GameEngine {

    fun applyHostShot(state: GameState, cellIndex: Int): GameState {
        require(state.status == GameStatus.HOST_TURN) { "Host turn expected" }
        require(cellIndex !in state.hostShotsMade) { "Cell already targeted" }

        var updatedShotsMade = state.hostShotsMade + cellIndex
        var updatedGuestShotsReceived = state.guestShotsReceived + cellIndex
        
        val hitShip = state.guestShips.find { cellIndex in it.cells }
        val isHit = hitShip != null
        
        if (isHit) {
            val isSunk = hitShip!!.cells.all { it in updatedGuestShotsReceived }
            if (isSunk) {
                val surrounding = getSurroundingCells(hitShip.cells)
                val newShots = surrounding.filter { it !in updatedShotsMade }
                updatedShotsMade = updatedShotsMade + newShots
                updatedGuestShotsReceived = updatedGuestShotsReceived + newShots
            }
        }

        val hostWon = allShipCells(state.guestShips).all(updatedGuestShotsReceived::contains)

        return state.copy(
            hostShotsMade = updatedShotsMade,
            guestShotsReceived = updatedGuestShotsReceived,
            currentTurnUid = when {
                hostWon -> state.hostUid
                isHit -> state.hostUid
                else -> state.guestUid.orEmpty()
            },
            status = when {
                hostWon -> GameStatus.FINISHED
                isHit -> GameStatus.HOST_TURN
                else -> GameStatus.GUEST_TURN
            },
            winnerUid = if (hostWon) state.hostUid else null,
            updatedAt = System.currentTimeMillis(),
        )
    }

    fun applyGuestShot(state: GameState, cellIndex: Int): GameState {
        require(state.status == GameStatus.GUEST_TURN) { "Guest turn expected" }
        require(cellIndex !in state.guestShotsMade) { "Cell already targeted" }

        var updatedShotsMade = state.guestShotsMade + cellIndex
        var updatedHostShotsReceived = state.hostShotsReceived + cellIndex
        
        val hitShip = state.hostShips.find { cellIndex in it.cells }
        val isHit = hitShip != null
        
        if (isHit) {
            val isSunk = hitShip!!.cells.all { it in updatedHostShotsReceived }
            if (isSunk) {
                val surrounding = getSurroundingCells(hitShip.cells)
                val newShots = surrounding.filter { it !in updatedShotsMade }
                updatedShotsMade = updatedShotsMade + newShots
                updatedHostShotsReceived = updatedHostShotsReceived + newShots
            }
        }

        val guestWon = allShipCells(state.hostShips).all(updatedHostShotsReceived::contains)

        return state.copy(
            guestShotsMade = updatedShotsMade,
            hostShotsReceived = updatedHostShotsReceived,
            currentTurnUid = when {
                guestWon -> state.guestUid.orEmpty()
                isHit -> state.guestUid.orEmpty()
                else -> state.hostUid
            },
            status = when {
                guestWon -> GameStatus.FINISHED
                isHit -> GameStatus.GUEST_TURN
                else -> GameStatus.HOST_TURN
            },
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

    private fun getSurroundingCells(shipCells: List<Int>): Set<Int> {
        val surrounding = mutableSetOf<Int>()
        for (cell in shipCells) {
            val row = cell / 10
            val col = cell % 10
            for (r in maxOf(0, row - 1)..minOf(9, row + 1)) {
                for (c in maxOf(0, col - 1)..minOf(9, col + 1)) {
                    val neighbor = r * 10 + c
                    if (neighbor !in shipCells) {
                        surrounding.add(neighbor)
                    }
                }
            }
        }
        return surrounding
    }
}
