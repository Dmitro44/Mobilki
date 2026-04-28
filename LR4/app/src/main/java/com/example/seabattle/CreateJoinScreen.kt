package com.example.seabattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seabattle.ui.theme.SeaBattleTheme

@Composable
fun CreateJoinScreen(
    playerName: String,
    joinCode: String,
    onJoinCodeChange: (String) -> Unit,
    onCreateLobbyClick: () -> Unit,
    onJoinLobbyClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
//        Text(
//            text = "Create or Join",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold
//        )
        Text(
            text = "Player: $playerName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isLoading) {
            LoadingState(text = "Working with lobby...")
        }

        if (errorMessage != null) {
            AppStateCard(title = "Lobby error", message = errorMessage)
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(text = "Create a new lobby")
                Text(
                    text = "Start a room and share the generated code with another player.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onCreateLobbyClick) {
                    Text("Create Lobby")
                }
            }
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(text = "Join with code")
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = onJoinCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lobby code") },
                    placeholder = { Text("AB12") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (joinCode.isNotBlank()) {
                                keyboardController?.hide()
                                onJoinLobbyClick()
                            }
                        }
                    ),
                )
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onJoinLobbyClick()
                    },
                    enabled = joinCode.isNotBlank()
                ) {
                    Text("Join Lobby")
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun CreateJoinScreenPreview() {
    SeaBattleTheme {
        CreateJoinScreen(
            playerName = "Cadet",
            joinCode = "AB12",
            onJoinCodeChange = {},
            onCreateLobbyClick = {},
            onJoinLobbyClick = {},
        )
    }
}
