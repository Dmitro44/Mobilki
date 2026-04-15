package com.example.timer.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.timer.data.local.entity.TimerPhase
import com.example.timer.data.local.entity.TimerSequence

data class TimerSequenceWithPhases(
    @Embedded
    val sequence: TimerSequence,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "sequence_id"
    )
    val phases: List<TimerPhase>
)
