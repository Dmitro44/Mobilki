package com.example.timer.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.timer.data.local.entity.TimerPhase
import com.example.timer.data.local.entity.TimerSequence

/**
 * Room relation class that joins TimerSequence with its TimerPhases
 * Used for @Transaction queries that fetch a sequence with all its phases
 */
data class TimerSequenceWithPhases(
    @Embedded
    val sequence: TimerSequence,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "sequence_id"
    )
    val phases: List<TimerPhase>
)
