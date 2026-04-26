package com.example.seabattle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seabattle.ui.theme.SeaBattleTheme

@Composable
fun HistoryScreen(
    matches: List<HistoryMatchUi>,
    onMatchClick: (HistoryMatchUi) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetryClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        when {
            isLoading -> LoadingState(text = "Loading match history...")
            errorMessage != null -> ErrorState(message = errorMessage, onRetryClick = onRetryClick)
            matches.isEmpty() -> EmptyState(
                title = "No matches yet",
                message = "Play a battle to see results here."
            )
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(matches, key = { it.id }) { match ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMatchClick(match) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "vs ${match.opponentName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = match.resultLabel,
                                    color = if (match.isWin) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                Text(
                                    text = "${match.dateLabel} • ${match.turns} turns",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    SeaBattleTheme {
        HistoryScreen(
            matches = listOf(
                HistoryMatchUi(
                    id = "1",
                    opponentName = "Skipper",
                    resultLabel = "Victory",
                    dateLabel = "Apr 26",
                    turns = 18,
                    isWin = true
                ),
                HistoryMatchUi(
                    id = "2",
                    opponentName = "Navigator",
                    resultLabel = "Defeat",
                    dateLabel = "Apr 24",
                    turns = 21,
                    isWin = false
                )
            ),
            onMatchClick = {}
        )
    }
}
