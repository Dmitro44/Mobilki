package com.example.timer.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.timer.R
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.getLocalizedName
import com.example.timer.service.PlaybackState
import com.example.timer.service.TimerState
import com.example.timer.ui.util.TimerServiceHelper
import com.example.timer.ui.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    sequenceId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    
    LaunchedEffect(sequenceId, timerState.playbackState) {
        if (sequenceId <= 0) return@LaunchedEffect
        
        when (timerState.playbackState) {
            PlaybackState.IDLE -> {
                TimerServiceHelper.startTimer(context, sequenceId)
            }
            PlaybackState.RUNNING, PlaybackState.PAUSED -> {
                if (timerState.sequenceId != sequenceId) {
                    TimerServiceHelper.stopTimer(context)
                    TimerServiceHelper.startTimer(context, sequenceId)
                }
            }
            else -> { }
        }
    }
    
    LaunchedEffect(timerState.playbackState) {
        if (timerState.playbackState == PlaybackState.COMPLETED) {
            kotlinx.coroutines.delay(2000)
            onNavigateBack()
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = timerState.sequenceName.ifBlank { "Timer" },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        TimerServiceHelper.stopTimer(context)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.dismiss)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getPhaseColor(timerState.currentPhaseType),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            PhaseInfoSection(
                timerState = timerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
            
            TimerDisplaySection(
                timerState = timerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            if (timerState.allPhases.isNotEmpty()) {
                PhaseQueueSection(
                    timerState = timerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            
            ControlsSection(
                timerState = timerState,
                onPlayPauseClick = {
                    when (timerState.playbackState) {
                        PlaybackState.RUNNING -> TimerServiceHelper.pauseTimer(context)
                        PlaybackState.PAUSED -> TimerServiceHelper.resumeTimer(context)
                        else -> {}
                    }
                },
                onStopClick = {
                    TimerServiceHelper.stopTimer(context)
                    onNavigateBack()
                },
                onSkipPreviousClick = { TimerServiceHelper.skipPrevious(context) },
                onSkipNextClick = { TimerServiceHelper.skipNext(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun PhaseInfoSection(
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getPhaseColor(timerState.currentPhaseType).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getPhaseColor(timerState.currentPhaseType)
            ) {
                val context = LocalContext.current
                Text(
                    text = timerState.currentPhaseType.getLocalizedName(context),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    label = stringResource(R.string.phase_label),
                    value = timerState.getPhaseDisplay()
                )
                InfoChip(
                    label = stringResource(R.string.rep_label),
                    value = timerState.getRepetitionDisplay()
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimerDisplaySection(
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = timerState.getFormattedRemainingTime(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = getPlaybackStateText(timerState.playbackState),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ControlsSection(
    timerState: TimerState,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onSkipPreviousClick,
                modifier = Modifier.size(56.dp),
                enabled = !timerState.isAtStart()
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = stringResource(R.string.skip),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            FloatingActionButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (timerState.playbackState == PlaybackState.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (timerState.playbackState == PlaybackState.RUNNING) {
                        stringResource(R.string.pause)
                    } else {
                        stringResource(R.string.resume)
                    },
                    modifier = Modifier.size(36.dp)
                )
            }
            
            FilledTonalIconButton(
                onClick = onSkipNextClick,
                modifier = Modifier.size(56.dp),
                enabled = !timerState.isAtEnd()
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(R.string.skip),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        OutlinedButton(
            onClick = onStopClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.stop_timer))
        }
    }
}

@Composable
private fun PhaseQueueSection(
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val queueItems = remember(timerState.allPhases) {
        timerState.allPhases.flatMapIndexed { phaseIndex, phase ->
            (0 until phase.repetitions).map { repIndex ->
                Triple(phaseIndex, repIndex, phase)
            }
        }
    }

    val currentGlobalIndex = queueItems.indexOfFirst { 
        it.first == timerState.currentPhaseIndex && it.second == timerState.currentRepetitionIndex 
    }

    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.next_phases),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            state = rememberLazyListState(initialFirstVisibleItemIndex = if (currentGlobalIndex > 0) currentGlobalIndex else 0)
        ) {
            itemsIndexed(queueItems) { index, (phaseIndex, repIndex, phase) ->
                val isCurrent = index == currentGlobalIndex
                val isPast = index < currentGlobalIndex
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrent) {
                        getPhaseColor(phase.phaseType).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .then(
                            if (isCurrent) Modifier.border(
                                2.dp,
                                getPhaseColor(phase.phaseType),
                                RoundedCornerShape(12.dp)
                            ) else Modifier
                        ),
                    contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceVariant).copy(
                        alpha = if (isPast) 0.5f else 1.0f
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(getPhaseColor(phase.phaseType), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phase.phaseType.getLocalizedName(context),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                        Text(
                            text = if (phase.repetitions > 1) {
                                "${repIndex + 1}/${phase.repetitions} • " + 
                                String.format("%02d:%02d", phase.durationSeconds / 60, phase.durationSeconds % 60)
                            } else {
                                String.format("%02d:%02d", phase.durationSeconds / 60, phase.durationSeconds % 60)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isPast) 0.5f else 1.0f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getPhaseColor(phaseType: PhaseType): Color {
    return when (phaseType) {
        PhaseType.WARMUP -> Color(0xFFFFA726)
        PhaseType.WORK -> Color(0xFFEF5350)
        PhaseType.REST -> Color(0xFF42A5F5)
        PhaseType.COOLDOWN -> Color(0xFF66BB6A)
    }
}

@Composable
private fun getPlaybackStateText(state: PlaybackState): String {
    return when (state) {
        PlaybackState.IDLE -> stringResource(R.string.state_ready)
        PlaybackState.RUNNING -> stringResource(R.string.state_running)
        PlaybackState.PAUSED -> stringResource(R.string.state_paused)
        PlaybackState.COMPLETED -> stringResource(R.string.state_completed)
    }
}
