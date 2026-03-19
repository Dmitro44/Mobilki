package com.example.timer.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model for a timer sequence
 * Decouples business logic from database entities
 */
data class TimerSequenceModel(
    val id: Long = 0,
    val name: String,
    val color: Color,
    val phases: List<TimerPhaseModel> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate total duration of all phases including repetitions
     */
    val totalDurationSeconds: Int
        get() = phases.sumOf { it.totalDurationSeconds }
    
    /**
     * Get a formatted duration string (HH:MM:SS or MM:SS)
     */
    fun getFormattedDuration(): String {
        val hours = totalDurationSeconds / 3600
        val minutes = (totalDurationSeconds % 3600) / 60
        val seconds = totalDurationSeconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    /**
     * Validate sequence data
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && phases.all { it.isValid() }
    }
    
    /**
     * Check if sequence has at least one phase
     */
    fun hasPhases(): Boolean = phases.isNotEmpty()
}
