package com.example.timer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a phase within a timer sequence
 * 
 * @property id Unique identifier, auto-generated
 * @property sequenceId Foreign key to parent TimerSequence
 * @property phaseType Type of phase (WARMUP, WORK, REST, COOLDOWN)
 * @property durationSeconds Duration of this phase in seconds
 * @property repetitions Number of times to repeat this phase
 * @property order Display order within the sequence
 */
@Entity(
    tableName = "timer_phases",
    foreignKeys = [
        ForeignKey(
            entity = TimerSequence::class,
            parentColumns = ["id"],
            childColumns = ["sequence_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sequence_id"])]
)
data class TimerPhase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "sequence_id")
    val sequenceId: Long,
    
    @ColumnInfo(name = "phase_type")
    val phaseType: String, // WARMUP, WORK, REST, COOLDOWN
    
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,
    
    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 1,
    
    @ColumnInfo(name = "order_index")
    val order: Int
)
