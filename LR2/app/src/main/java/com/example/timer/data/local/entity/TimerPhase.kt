package com.example.timer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val phaseType: String,
    
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,
    
    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 1,
    
    @ColumnInfo(name = "order_index")
    val order: Int
)
