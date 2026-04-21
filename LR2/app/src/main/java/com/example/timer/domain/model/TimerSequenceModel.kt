package com.example.timer.domain.model

import androidx.compose.ui.graphics.Color

import java.util.Locale

data class TimerSequenceModel(
    val id: Long = 0,
    val name: String,
    val color: Color,
    val phases: List<TimerPhaseModel> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDurationSeconds: Int
        get() = phases.sumOf { it.totalDurationSeconds }
    
    fun getFormattedDuration(): String {
        val hours = totalDurationSeconds / 3600
        val minutes = (totalDurationSeconds % 3600) / 60
        val seconds = totalDurationSeconds % 60
        
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
    
    fun isValid(): Boolean {
        return name.isNotBlank() && phases.all { it.isValid() }
    }
    
    fun hasPhases(): Boolean = phases.isNotEmpty()
}
