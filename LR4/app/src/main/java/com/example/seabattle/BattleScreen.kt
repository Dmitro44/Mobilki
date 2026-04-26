package com.example.seabattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seabattle.ui.theme.SeaBattleTheme

@Composable
fun BattleScreen(
    ownBoard: List<List<BoardCellState>>,
    enemyBoard: List<List<BoardCellState>>,
    currentTurnText: String,
    statusMessage: String,
    onEnemyCellClick: (row: Int, column: Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedEnemyCell: BoardPosition? = null,
    isYourTurn: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onFinishClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Battle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            LoadingState(text = "Updating battle state...")
        }

        if (errorMessage != null) {
            AppStateCard(title = "Battle error", message = errorMessage)
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentTurnText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(text = "Your board")
            BoardGrid(board = ownBoard)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(text = "Enemy board")
            BoardGrid(
                board = enemyBoard,
                enabled = isYourTurn && !isLoading,
                selectedCell = selectedEnemyCell,
                onCellClick = onEnemyCellClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isYourTurn) "Select target" else "Opponent turn")
            }
            OutlinedButton(
                onClick = onFinishClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Exit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BattleScreenPreview() {
    val ownBoard = listOf(
        listOf(BoardCellState.Ship, BoardCellState.Ship, BoardCellState.Empty, BoardCellState.Empty),
        listOf(BoardCellState.Empty, BoardCellState.Hit, BoardCellState.Empty, BoardCellState.Empty),
        listOf(BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Miss, BoardCellState.Empty),
        listOf(BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty)
    )
    val enemyBoard = listOf(
        listOf(BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Miss),
        listOf(BoardCellState.Empty, BoardCellState.Hit, BoardCellState.Empty, BoardCellState.Empty),
        listOf(BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty),
        listOf(BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty, BoardCellState.Empty)
    )

    SeaBattleTheme {
        BattleScreen(
            ownBoard = ownBoard,
            enemyBoard = enemyBoard,
            currentTurnText = "Your turn",
            statusMessage = "Tap a cell on the enemy board to make a move.",
            selectedEnemyCell = BoardPosition(0, 2),
            isYourTurn = true,
            onEnemyCellClick = { _, _ -> }
        )
    }
}
