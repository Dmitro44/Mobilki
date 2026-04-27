package com.example.seabattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    showGameResultDialog: Boolean = false,
    gameResultTitle: String = "Game finished",
    gameResultMessage: String = "",
    onDismissGameResult: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    if (showGameResultDialog) {
        AlertDialog(
            onDismissRequest = onDismissGameResult,
            title = { Text(gameResultTitle) },
            text = { Text(gameResultMessage) },
            confirmButton = {
                TextButton(onClick = onDismissGameResult) {
                    Text("Back to main menu")
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Battle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

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
                Text(
                    text = "Rule: a hit lets the same player shoot again. A miss passes the turn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(text = "Your board")
                Text(
                    text = "Your ships stay visible here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BoardGrid(board = ownBoard)
            }
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(text = "Enemy board")
                Text(
                    text = if (isYourTurn) {
                        "Select a target. Keep shooting while you keep hitting."
                    } else {
                        "Wait here until the opponent misses or the battle ends."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BoardGrid(
                    board = enemyBoard,
                    enabled = isYourTurn && !isLoading,
                    selectedCell = selectedEnemyCell,
                    onCellClick = onEnemyCellClick
                )
            }
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
                Text(
                    when {
                        isLoading -> "Updating board"
                        isYourTurn -> "Shoot until miss"
                        else -> "Opponent turn"
                    }
                )
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
    val ownBoard = previewBoard(
        shipCells = listOf(
            BoardPosition(1, 1),
            BoardPosition(1, 2),
            BoardPosition(1, 3),
            BoardPosition(4, 6),
            BoardPosition(5, 6),
        ),
        hitCells = listOf(BoardPosition(5, 6)),
        sunkCells = listOf(
            BoardPosition(1, 1),
            BoardPosition(1, 2),
            BoardPosition(1, 3),
        ),
        missCells = listOf(BoardPosition(7, 7)),
    )
    val enemyBoard = previewBoard(
        hitCells = listOf(BoardPosition(2, 5)),
        sunkCells = listOf(BoardPosition(2, 6), BoardPosition(3, 6)),
        missCells = listOf(BoardPosition(0, 9), BoardPosition(6, 4)),
    )

    SeaBattleTheme {
        BattleScreen(
            ownBoard = ownBoard,
            enemyBoard = enemyBoard,
            currentTurnText = "Your turn",
            statusMessage = "Choose a cell on the enemy board. A hit keeps your turn alive.",
            selectedEnemyCell = BoardPosition(2, 4),
            isYourTurn = true,
            showGameResultDialog = true,
            gameResultTitle = "Victory",
            gameResultMessage = "You destroyed the enemy fleet.",
            onEnemyCellClick = { _, _ -> }
        )
    }
}

private fun previewBoard(
    shipCells: List<BoardPosition> = emptyList(),
    hitCells: List<BoardPosition> = emptyList(),
    sunkCells: List<BoardPosition> = emptyList(),
    missCells: List<BoardPosition> = emptyList(),
): List<List<BoardCellState>> {
    return List(10) { row ->
        List(10) { column ->
            val position = BoardPosition(row, column)
            when {
                position in sunkCells -> BoardCellState.Sunk
                position in hitCells -> BoardCellState.Hit
                position in missCells -> BoardCellState.Miss
                position in shipCells -> BoardCellState.Ship
                else -> BoardCellState.Empty
            }
        }
    }
}
