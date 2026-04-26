package com.example.seabattle.model

data class GameSummary(
    val gameId: String = "",
    val opponentName: String = "",
    val didWin: Boolean = false,
    val finishedAt: Long = 0L,
    val totalTurns: Int = 0,
)
