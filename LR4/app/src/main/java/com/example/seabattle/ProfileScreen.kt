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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seabattle.model.AvatarChoice
import com.example.seabattle.ui.theme.SeaBattleTheme

@Composable
fun ProfileScreen(
    playerName: String,
    selectedAvatar: AvatarChoice,
    avatarOptions: List<AvatarChoice>,
    gamesPlayed: Int,
    wins: Int,
    losses: Int,
    onNameChange: (String) -> Unit,
    onAvatarSelected: (AvatarChoice) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (isSaving) {
            LoadingState(text = "Saving profile...")
        }

        if (errorMessage != null) {
            AppStateCard(
                title = "Profile error",
                message = errorMessage
            )
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Player name") },
                    singleLine = true
                )

                SectionTitle(text = "Avatar")
                AvatarChoiceRow(
                    selectedAvatar = selectedAvatar,
                    avatarOptions = avatarOptions,
                    onAvatarSelected = onAvatarSelected
                )

                Button(
                    onClick = onSaveClick,
                    enabled = playerName.isNotBlank() && !isSaving,
                ) {
                    Text("Save Profile")
                }
            }
        }

        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Games", value = gamesPlayed.toString())
                StatItem(label = "Wins", value = wins.toString())
                StatItem(label = "Losses", value = losses.toString())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    SeaBattleTheme {
        ProfileScreen(
            playerName = "Cadet",
            selectedAvatar = AvatarChoice.CAPTAIN,
            avatarOptions = AvatarChoice.entries,
            gamesPlayed = 12,
            wins = 7,
            losses = 5,
            onNameChange = {},
            onAvatarSelected = {},
            onSaveClick = {}
        )
    }
}
