package com.example.timer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.timer.R
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhaseModel
import com.example.timer.ui.viewmodel.EditViewModel
import kotlinx.coroutines.launch

/**
 * Screen for creating and editing timer sequences
 * 
 * Features:
 * - Text field for sequence name
 * - Color picker grid
 * - Phase list with add/remove/reorder
 * - Duration and repetition inputs for each phase
 * - Save/Cancel actions
 * 
 * @param viewModel ViewModel managing edit state
 * @param sequenceId ID of sequence to edit (null for create mode)
 * @param onNavigateBack Callback when user cancels or successfully saves
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel,
    sequenceId: Long?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // Load sequence when screen appears
    LaunchedEffect(sequenceId) {
        viewModel.loadSequence(sequenceId)
    }
    
    // Navigate back on successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (sequenceId == null || sequenceId == 0L) stringResource(R.string.create_sequence) else stringResource(R.string.edit_sequence),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveSequence()
                            }
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.save)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sequence name input
            item {
                NameInput(
                    name = uiState.name,
                    onNameChange = viewModel::updateName
                )
            }
            
            // Color picker
            item {
                ColorPicker(
                    selectedColor = uiState.selectedColor,
                    onColorSelected = viewModel::updateColor
                )
            }
            
            // Phases section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.phases, uiState.phases.size),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalButton(
                        onClick = { viewModel.addPhase() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_phase))
                    }
                }
            }
            
            // Phase list
            itemsIndexed(
                items = uiState.phases,
                key = { index, _ -> index }
            ) { index, phase ->
                PhaseCard(
                    phase = phase,
                    index = index,
                    totalPhases = uiState.phases.size,
                    onUpdate = { updatedPhase -> viewModel.updatePhase(index, updatedPhase) },
                    onRemove = { viewModel.removePhase(index) },
                    onMoveUp = { viewModel.movePhaseUp(index) },
                    onMoveDown = { viewModel.movePhaseDown(index) }
                )
            }
            
            // Error message
            uiState.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Name input field
 */
@Composable
private fun NameInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.sequence_name)) },
        placeholder = { Text("e.g., HIIT Workout") },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
        }
    )
}

/**
 * Color picker grid
 */
@Composable
private fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF6200EE), // Purple
        Color(0xFF3700B3), // Dark Purple
        Color(0xFF03DAC6), // Teal
        Color(0xFF018786), // Dark Teal
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF9800), // Orange
        Color(0xFF795548)  // Brown
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.color),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        // Color grid (3 columns)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.chunked(4).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowColors.forEach { color ->
                        ColorCircle(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { onColorSelected(color) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual color circle
 */
@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.resume),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Card for editing a phase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhaseCard(
    phase: TimerPhaseModel,
    index: Int,
    totalPhases: Int,
    onUpdate: (TimerPhaseModel) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with phase number and controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.phase_num, index + 1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Move up button
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.skip)
                        )
                    }
                    
                    // Move down button
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalPhases - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.skip)
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_phase),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Phase type dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = phase.phaseType.name,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text(stringResource(R.string.type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    PhaseType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                onUpdate(phase.copy(phaseType = type))
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Duration and repetitions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duration input
                OutlinedTextField(
                    value = if (phase.durationSeconds == 0) "" else phase.durationSeconds.toString(),
                    onValueChange = { value ->
                        if (value.isEmpty()) {
                            onUpdate(phase.copy(durationSeconds = 0))
                        } else {
                            value.toIntOrNull()?.let { seconds ->
                                if (seconds >= 0) {
                                    onUpdate(phase.copy(durationSeconds = seconds))
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.duration_sec)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // Repetitions input
                OutlinedTextField(
                    value = if (phase.repetitions == 0) "" else phase.repetitions.toString(),
                    onValueChange = { value ->
                        if (value.isEmpty()) {
                            onUpdate(phase.copy(repetitions = 0))
                        } else {
                            value.toIntOrNull()?.let { reps ->
                                if (reps >= 0) {
                                    onUpdate(phase.copy(repetitions = reps))
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.repetitions)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            // Total duration info
            Text(
                text = stringResource(R.string.total_phase_duration, phase.totalDurationSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_phase)) },
            text = { Text(stringResource(R.string.delete_phase_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
