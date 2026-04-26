package com.example.seabattle.game

import com.example.seabattle.model.GameState
import com.example.seabattle.model.GameStatus
import com.example.seabattle.model.Profile
import com.example.seabattle.model.Ship
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun applyHostShot_switchesTurnWhenGameContinues() {
        val state = sampleGameState()

        val updated = GameEngine.applyHostShot(state, 20)

        assertEquals(GameStatus.GUEST_TURN, updated.status)
        assertEquals("guest", updated.currentTurnUid)
        assertTrue(20 in updated.hostShotsMade)
        assertTrue(20 in updated.guestShotsReceived)
    }

    @Test
    fun applyGuestShot_marksWinnerWhenAllHostCellsHit() {
        val state = sampleGameState(
            status = GameStatus.GUEST_TURN,
            currentTurnUid = "guest",
            hostShotsReceived = listOf(0, 1, 2, 5),
            guestShotsMade = listOf(0, 1, 2, 5),
        )

        val updated = GameEngine.applyGuestShot(state, 6)

        assertEquals(GameStatus.FINISHED, updated.status)
        assertEquals("guest", updated.winnerUid)
        assertTrue(6 in updated.hostShotsReceived)
    }

    private fun sampleGameState(
        status: GameStatus = GameStatus.HOST_TURN,
        currentTurnUid: String = "host",
        hostShotsReceived: List<Int> = emptyList(),
        guestShotsMade: List<Int> = emptyList(),
    ): GameState {
        return GameState(
            gameId = "ABC123",
            hostUid = "host",
            guestUid = "guest",
            hostProfile = Profile(uid = "host", nickname = "Host"),
            guestProfile = Profile(uid = "guest", nickname = "Guest"),
            status = status,
            currentTurnUid = currentTurnUid,
            hostReady = true,
            guestReady = true,
            hostShips = listOf(Ship(3, listOf(0, 1, 2)), Ship(2, listOf(5, 6))),
            guestShips = listOf(Ship(3, listOf(10, 11, 12)), Ship(2, listOf(15, 16))),
            hostShotsReceived = hostShotsReceived,
            guestShotsMade = guestShotsMade,
        )
    }
}
