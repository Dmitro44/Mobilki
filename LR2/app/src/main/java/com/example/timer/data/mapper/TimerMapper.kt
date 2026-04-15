package com.example.timer.data.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.timer.data.local.entity.TimerPhase
import com.example.timer.data.local.entity.TimerSequence
import com.example.timer.data.local.relation.TimerSequenceWithPhases
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhaseModel
import com.example.timer.domain.model.TimerSequenceModel


fun TimerSequence.toModel(phases: List<TimerPhaseModel> = emptyList()): TimerSequenceModel {
    return TimerSequenceModel(
        id = id,
        name = name,
        color = Color(colorArgb),
        phases = phases,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TimerSequenceModel.toEntity(): TimerSequence {
    return TimerSequence(
        id = id,
        name = name,
        colorArgb = color.toArgb(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TimerPhase.toModel(): TimerPhaseModel {
    return TimerPhaseModel(
        id = id,
        sequenceId = sequenceId,
        phaseType = PhaseType.fromString(phaseType),
        durationSeconds = durationSeconds,
        repetitions = repetitions,
        order = order
    )
}

fun TimerPhaseModel.toEntity(): TimerPhase {
    return TimerPhase(
        id = id,
        sequenceId = sequenceId,
        phaseType = phaseType.name,
        durationSeconds = durationSeconds,
        repetitions = repetitions,
        order = order
    )
}

fun TimerSequenceWithPhases.toModel(): TimerSequenceModel {
    return sequence.toModel(
        phases = phases.map { it.toModel() }
    )
}

fun List<TimerSequence>.toModelList(phasesMap: Map<Long, List<TimerPhaseModel>> = emptyMap()): List<TimerSequenceModel> {
    return map { sequence ->
        sequence.toModel(phasesMap[sequence.id] ?: emptyList())
    }
}

fun List<TimerSequenceWithPhases>.toModelList(): List<TimerSequenceModel> {
    return map { it.toModel() }
}

fun List<TimerPhase>.toPhaseModelList(): List<TimerPhaseModel> {
    return map { it.toModel() }
}

fun List<TimerPhaseModel>.toPhaseEntityList(): List<TimerPhase> {
    return map { it.toEntity() }
}
