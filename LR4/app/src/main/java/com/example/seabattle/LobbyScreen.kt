package com.example.seabattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seabattle.ui.theme.SeaBattleTheme

@Composable
fun LobbyScreen(
    lobbyCode: String,
    players: List<LobbyPlayerUi>,
    currentPlayerName: String,
    isCurrentPlayerReady: Boolean,
    onReadyChange: (Boolean) -> Unit,
    onShareCodeClick: () -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentPlayerHost: Boolean = false,
    canStart: Boolean = false,
    isStarting: Boolean = false,
    statusMessage: String = "Waiting for players to get ready.",
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Lobby",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (isStarting) {
            LoadingState(text = "Starting battle...")
        }

        if (errorMessage != null) {
            AppStateCard(title = "Lobby error", message = errorMessage)
        }

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

        SectionTitle(text = "Players")

        players.forEach { player ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${player.avatar} ${player.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        val roleLabel = buildString {
                            if (player.isHost) append("Host")
                            if (player.name == currentPlayerName) {
                                if (isNotEmpty()) append(" • ")
                                append("You")
                            }
                        }
                        if (roleLabel.isNotEmpty()) {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = isCurrentPlayerReady,
                    onCheckedChange = onReadyChange,
                    enabled = !isCurrentPlayerReady,
                )
                Text(text = "Ready to play")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onLeaveClick) {
                Text("Leave")
            }
            if (isCurrentPlayerHost && canStart) {
                Text(
                    text = "Battle starts automatically when both players are ready.",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                LobbyPlayerUi(name = "Cadet", avatar = "🚢", isReady = true, isHost = true),
                LobbyPlayerUi(name = "Skipper", avatar = "⚓", isReady = false)
            ),
            currentPlayerName = "Cadet",
            isCurrentPlayerReady = true,
            onReadyChange = {},
            onShareCodeClick = {},
            onLeaveClick = {},
            isCurrentPlayerHost = true,
            canStart = false
        )
    }
}
