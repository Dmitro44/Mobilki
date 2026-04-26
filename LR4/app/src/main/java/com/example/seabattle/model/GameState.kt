package com.example.seabattle.model

data class GameState(
    val gameId: String = "",
    val hostUid: String = "",
    val guestUid: String? = null,
    val hostProfile: Profile? = null,
    val guestProfile: Profile? = null,
    val status: GameStatus = GameStatus.WAITING_FOR_GUEST,
    val currentTurnUid: String = "",
    val hostReady: Boolean = false,
    val guestReady: Boolean = false,
    val hostShips: List<Ship> = emptyList(),
    val guestShips: List<Ship> = emptyList(),
    val hostShotsReceived: List<Int> = emptyList(),
    val guestShotsReceived: List<Int> = emptyList(),
    val hostShotsMade: List<Int> = emptyList(),
    val guestShotsMade: List<Int> = emptyList(),
    val pendingGuestShot: PendingShot? = null,
    val lastProcessedGuestRequestId: String? = null,
    val winnerUid: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val isFinished: Boolean
        get() = status == GameStatus.FINISHED && winnerUid != null

    val canStartBattle: Boolean
        get() = hostReady && guestReady && guestUid != null
}
