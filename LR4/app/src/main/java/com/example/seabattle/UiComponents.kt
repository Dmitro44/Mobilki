package com.example.seabattle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.seabattle.model.AvatarChoice

@Composable
fun AppStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun LoadingState(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppStateCard(
        title = "Something went wrong",
        message = message,
        modifier = modifier,
        actionLabel = "Retry",
        onActionClick = onRetryClick
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    AppStateCard(
        title = title,
        message = message,
        modifier = modifier,
        actionLabel = actionLabel,
        onActionClick = onActionClick
    )
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun AvatarChoiceRow(
    selectedAvatar: AvatarChoice,
    avatarOptions: List<AvatarChoice>,
    onAvatarSelected: (AvatarChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        avatarOptions.forEach { avatar ->
            Card(
                modifier = Modifier.clickable { onAvatarSelected(avatar) },
                colors = CardDefaults.cardColors(
                    containerColor = if (avatar == selectedAvatar) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Image(
                        painter = painterResource(avatar.drawableResId),
                        contentDescription = avatar.title,
                        modifier = Modifier.size(56.dp),
                    )
                    Text(text = avatar.title)
                }
            }
        }
    }
}

@Composable
fun PlayerAvatar(
    avatarChoice: AvatarChoice,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(avatarChoice.drawableResId),
            contentDescription = avatarChoice.title,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun PlayerIdentity(
    name: String,
    avatarChoice: AvatarChoice,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    avatarSize: Dp = 40.dp,
    nameTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(
            avatarChoice = avatarChoice,
            size = avatarSize,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = name,
                style = nameTextStyle,
                fontWeight = FontWeight.Medium,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun BoardGrid(
    board: List<List<BoardCellState>>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedCell: BoardPosition? = null,
    onCellClick: ((row: Int, column: Int) -> Unit)? = null
) {
    if (board.isEmpty() || board.firstOrNull().isNullOrEmpty()) {
        EmptyState(
            title = "Board unavailable",
            message = "No board data was provided.",
            modifier = modifier
        )
        return
    }

    val columnCount = board.maxOf { it.size }
    val cellSpacing = if (columnCount >= 10) 1.dp else 4.dp
    val rowLabelWidth = if (columnCount >= 10) 16.dp else 24.dp
    val minimumCellSize = if (columnCount >= 10) 24.dp else 32.dp
    val maximumCellSize = 36.dp
    val cellShape = RoundedCornerShape(if (columnCount >= 10) 4.dp else 6.dp)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalSpacing = cellSpacing * (columnCount - 1)
        val availableWidth = (this.maxWidth - rowLabelWidth - totalSpacing)
            .coerceAtLeast(minimumCellSize * columnCount)
        val cellSize = (availableWidth / columnCount).coerceIn(minimumCellSize, maximumCellSize)

        Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.width(rowLabelWidth))
                repeat(columnCount) { columnIndex ->
                    Text(
                        text = (columnIndex + 1).toString(),
                        modifier = Modifier.width(cellSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            board.forEachIndexed { rowIndex, row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = boardRowLabel(rowIndex),
                        modifier = Modifier.width(rowLabelWidth),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    row.forEachIndexed { columnIndex, cellState ->
                        val isSelected = selectedCell?.row == rowIndex && selectedCell.column == columnIndex
                        val backgroundColor = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            cellState == BoardCellState.Empty -> MaterialTheme.colorScheme.surfaceVariant
                            cellState == BoardCellState.Ship -> MaterialTheme.colorScheme.secondaryContainer
                            cellState == BoardCellState.Hit -> MaterialTheme.colorScheme.errorContainer
                            cellState == BoardCellState.Sunk -> MaterialTheme.colorScheme.error
                            cellState == BoardCellState.Miss -> MaterialTheme.colorScheme.tertiaryContainer
                            cellState == BoardCellState.Disabled -> MaterialTheme.colorScheme.surface
                            cellState == BoardCellState.Selected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val contentColor = when (cellState) {
                            BoardCellState.Hit -> MaterialTheme.colorScheme.onErrorContainer
                            BoardCellState.Sunk -> MaterialTheme.colorScheme.onError
                            BoardCellState.Miss -> MaterialTheme.colorScheme.onTertiaryContainer
                            BoardCellState.Ship -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val borderColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            cellState == BoardCellState.Sunk -> MaterialTheme.colorScheme.onError
                            else -> MaterialTheme.colorScheme.outline
                        }
                        val interactionSource = remember(rowIndex, columnIndex) { MutableInteractionSource() }
                        val clickableModifier = if (enabled && onCellClick != null) {
                            Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    bounded = true,
                                    radius = cellSize / 2,
                                ),
                            ) {
                                onCellClick(rowIndex, columnIndex)
                            }
                        } else {
                            Modifier
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(cellShape)
                                .background(backgroundColor)
                                .border(
                                    width = 1.dp,
                                    color = borderColor,
                                    shape = cellShape,
                                )
                                .then(clickableModifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            val symbol = when {
                                isSelected -> "•"
                                cellState == BoardCellState.Sunk -> "■"
                                cellState == BoardCellState.Hit -> "X"
                                cellState == BoardCellState.Miss -> "•"
                                cellState == BoardCellState.Ship -> "■"
                                else -> ""
                            }
                            Text(
                                text = symbol,
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun boardRowLabel(rowIndex: Int): String {
    return ('A'.code + rowIndex).toChar().toString()
}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DividerSpacer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
