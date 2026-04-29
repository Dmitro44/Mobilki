package com.example.seabattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seabattle.game.FleetRules
import com.example.seabattle.model.AvatarChoice
import com.example.seabattle.model.Ship
import com.example.seabattle.model.ShipOrientation
import com.example.seabattle.ui.theme.SeaBattleTheme

@Composable
fun LobbyScreen(
    lobbyCode: String,
    players: List<LobbyPlayerUi>,
    currentPlayerName: String,
    isCurrentPlayerReady: Boolean,
    placedShips: List<Ship>,
    remainingShipSizes: List<Int>,
    selectedShipSize: Int?,
    shipOrientation: ShipOrientation,
    onReadyButtonClick: () -> Unit,
    onSelectShipSize: (Int) -> Unit,
    onToggleOrientation: () -> Unit,
    onBoardCellClick: (row: Int, column: Int) -> Unit,
    onClearPlacementClick: () -> Unit,
    onShareCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentPlayerHost: Boolean = false,
    canStart: Boolean = false,
    isStarting: Boolean = false,
    statusMessage: String = "Waiting for players to get ready.",
    errorMessage: String? = null,
) {
    val currentShipSize = selectedShipSize ?: remainingShipSizes.firstOrNull()
    val placementBoard = buildPlacementBoard(placedShips)
    val canMarkReady = remainingShipSizes.isEmpty() && !isCurrentPlayerReady
    val shipOptions = listOf(4, 3, 2, 1)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isStarting) {
            item {
                LoadingState(text = "Starting battle...")
            }
        }

        if (errorMessage != null) {
            item {
                AppStateCard(title = "Lobby error", message = errorMessage)
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lobby code: $lobbyCode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onShareCodeClick) {
                        Text("Share code")
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionTitle(text = "Ship placement")
                    Text(
                        text = "Place ships on the 10×10 board before marking yourself ready.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = shipOrientation == ShipOrientation.HORIZONTAL,
                            onClick = {
                                if (shipOrientation != ShipOrientation.HORIZONTAL) {
                                    onToggleOrientation()
                                }
                            },
                            enabled = !isCurrentPlayerReady,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = !isCurrentPlayerReady,
                                selected = shipOrientation == ShipOrientation.HORIZONTAL,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 3.dp,
                            ),
                            label = { Text("Horizontal") }
                        )
                        FilterChip(
                            selected = shipOrientation == ShipOrientation.VERTICAL,
                            onClick = {
                                if (shipOrientation != ShipOrientation.VERTICAL) {
                                    onToggleOrientation()
                                }
                            },
                            enabled = !isCurrentPlayerReady,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = !isCurrentPlayerReady,
                                selected = shipOrientation == ShipOrientation.VERTICAL,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 3.dp,
                            ),
                            label = { Text("Vertical") }
                        )
                    }
                    Column(
                        modifier = Modifier.heightIn(min = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        shipOptions.chunked(2).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowOptions.forEach { size ->
                                    val count = remainingShipSizes.count { it == size }
                                    FilterChip(
                                        selected = currentShipSize == size,
                                        onClick = { onSelectShipSize(size) },
                                        enabled = !isCurrentPlayerReady && count > 0,
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = !isCurrentPlayerReady && count > 0,
                                            selected = currentShipSize == size,
                                            borderColor = MaterialTheme.colorScheme.outline,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                                            borderWidth = 1.dp,
                                            selectedBorderWidth = 3.dp,
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .widthIn(min = 0.dp),
                                        label = { Text("$size ×$count") },
                                    )
                                }
                            }
                        }
                    }
                    BoardGrid(
                        board = placementBoard,
                        enabled = !isCurrentPlayerReady,
                        onCellClick = onBoardCellClick,
                    )
                    Text(
                        text = if (currentShipSize != null) {
                            "Current ship: $currentShipSize cells"
                        } else {
                            "Current ship: all ships placed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = shipOptions.joinToString(prefix = "Remaining: ") { size ->
                            "$size×${remainingShipSizes.count { it == size }}"
                        },
                        modifier = Modifier.heightIn(min = 20.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    OutlinedButton(
                        onClick = onClearPlacementClick,
                        enabled = placedShips.isNotEmpty() && !isCurrentPlayerReady,
                    ) {
                        Text("Clear placement")
                    }
                }
            }
        }

        item {
            SectionTitle(text = "Players")
        }

        items(players) { player ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val roleLabel = buildString {
                        if (player.isHost) append("Host")
                        if (player.name == currentPlayerName) {
                            if (isNotEmpty()) append(" • ")
                            append("You")
                        }
                    }
                    PlayerIdentity(
                        name = player.name,
                        avatarChoice = player.avatar,
                        modifier = Modifier.weight(1f),
                        supportingText = roleLabel.ifEmpty { null },
                        avatarSize = 44.dp,
                    )
                    Text(
                        text = if (player.isReady) "Ready" else "Not ready",
                        color = if (player.isReady) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReadyButtonClick,
                        enabled = isCurrentPlayerReady || canMarkReady,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isCurrentPlayerReady) "Become unready" else "Ready to play")
                    }
                    if (!canMarkReady && !isCurrentPlayerReady) {
                        Text(
                            text = "Place every ship to enable ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isCurrentPlayerReady) {
                        Text(
                            text = "You can become unready to edit your ships again before the battle starts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (isCurrentPlayerHost && canStart) {
            item {
                Text(
                    text = "Battle starts automatically when both players are ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun buildPlacementBoard(ships: List<Ship>): List<List<BoardCellState>> {
    val shipCells = ships.flatMap { it.cells }.toSet()
    return List(FleetRules.BOARD_SIZE) { row ->
        List(FleetRules.BOARD_SIZE) { column ->
            if (FleetRules.cellIndex(row, column) in shipCells) {
                BoardCellState.Ship
            } else {
                BoardCellState.Empty
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LobbyScreenPreview() {
    SeaBattleTheme {
        LobbyScreen(
            lobbyCode = "AB12",
            players = listOf(
                LobbyPlayerUi(name = "Player 1", avatar = AvatarChoice.VRUNGEL, isReady = true, isHost = true),
                LobbyPlayerUi(name = "Player 2", avatar = AvatarChoice.PAPAI, isReady = false)
            ),
            currentPlayerName = "Player 1",
            isCurrentPlayerReady = false,
            placedShips = emptyList(),
            remainingShipSizes = FleetRules.REQUIRED_SHIP_SIZES,
            selectedShipSize = 4,
            shipOrientation = ShipOrientation.HORIZONTAL,
            onReadyButtonClick = {},
            onSelectShipSize = {},
            onToggleOrientation = {},
            onBoardCellClick = { _, _ -> },
            onClearPlacementClick = {},
            onShareCodeClick = {},
            isCurrentPlayerHost = true,
            canStart = false
        )
    }
}
