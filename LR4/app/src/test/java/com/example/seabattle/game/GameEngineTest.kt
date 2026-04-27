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
    fun applyHostShot_keepsTurnWhenHit() {
        val state = sampleGameState()

        val updated = GameEngine.applyHostShot(state, 10)

        assertEquals(GameStatus.HOST_TURN, updated.status)
        assertEquals("host", updated.currentTurnUid)
        assertTrue(10 in updated.hostShotsMade)
        assertTrue(10 in updated.guestShotsReceived)
    }

    @Test
    fun applyHostShot_switchesTurnWhenMiss() {
        val state = sampleGameState()

        val updated = GameEngine.applyHostShot(state, 99)

        assertEquals(GameStatus.GUEST_TURN, updated.status)
        assertEquals("guest", updated.currentTurnUid)
        assertTrue(99 in updated.hostShotsMade)
    }

    @Test
    fun applyGuestShot_keepsTurnWhenHit() {
        val state = sampleGameState(
            status = GameStatus.GUEST_TURN,
            currentTurnUid = "guest",
        )

        val updated = GameEngine.applyGuestShot(state, 0)

        assertEquals(GameStatus.GUEST_TURN, updated.status)
        assertEquals("guest", updated.currentTurnUid)
        assertTrue(0 in updated.hostShotsReceived)
    }

    @Test
    fun applyGuestShot_marksWinnerWhenAllHostCellsHit() {
        val state = sampleGameState(
            status = GameStatus.GUEST_TURN,
            currentTurnUid = "guest",
            hostShotsReceived = listOf(0, 1, 2, 10, 11, 12, 20, 21, 22, 30, 31, 40, 41, 50, 51, 60, 62, 64, 66),
            guestShotsMade = listOf(0, 1, 2, 10, 11, 12, 20, 21, 22, 30, 31, 40, 41, 50, 51, 60, 62, 64, 66),
        )

        val updated = GameEngine.applyGuestShot(state, 3)

        assertEquals(GameStatus.FINISHED, updated.status)
        assertEquals("guest", updated.winnerUid)
        assertTrue(3 in updated.hostShotsReceived)
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
            hostShips = listOf(
                Ship(4, listOf(0, 1, 2, 3)),
                Ship(3, listOf(10, 11, 12)),
                Ship(3, listOf(20, 21, 22)),
                Ship(2, listOf(30, 31)),
                Ship(2, listOf(40, 41)),
                Ship(2, listOf(50, 51)),
                Ship(1, listOf(60)),
                Ship(1, listOf(62)),
                Ship(1, listOf(64)),
                Ship(1, listOf(66)),
            ),
            guestShips = listOf(
                Ship(4, listOf(10, 11, 12, 13)),
                Ship(3, listOf(20, 21, 22)),
                Ship(3, listOf(30, 31, 32)),
                Ship(2, listOf(40, 41)),
                Ship(2, listOf(50, 51)),
                Ship(2, listOf(60, 61)),
                Ship(1, listOf(70)),
                Ship(1, listOf(72)),
                Ship(1, listOf(74)),
                Ship(1, listOf(76)),
            ),
            hostShotsReceived = hostShotsReceived,
            guestShotsMade = guestShotsMade,
        )
    }
}
