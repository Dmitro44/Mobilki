package com.example.seabattle

data class LobbyPlayerUi(
    val name: String,
    val avatar: String,
    val isReady: Boolean,
    val isHost: Boolean = false
)

data class HistoryMatchUi(
    val id: String,
    val opponentName: String,
    val resultLabel: String,
    val dateLabel: String,
    val turns: Int,
    val isWin: Boolean
)

data class BoardPosition(
    val row: Int,
    val column: Int
)

enum class BoardCellState {
    Empty,
    Ship,
    Hit,
    Miss,
    Disabled,
    Selected
}
