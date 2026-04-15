package com.example.timer.service

import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhaseModel

data class TimerState(
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val sequenceId: Long = 0,
    val sequenceName: String = "",
    val currentPhaseIndex: Int = 0,
    val currentRepetitionIndex: Int = 0,
    val totalRepetitions: Int = 1,
    val currentPhaseType: PhaseType = PhaseType.WORK,
    val totalPhases: Int = 0,
    val allPhases: List<TimerPhaseModel> = emptyList(),
    val remainingSeconds: Int = 0,
    val phaseDurationSeconds: Int = 0
) {
    fun getFormattedRemainingTime(): String {
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun getPhaseProgress(): Float {
        if (phaseDurationSeconds <= 0) return 0f
        val elapsed = phaseDurationSeconds - remainingSeconds
        return (elapsed.toFloat() / phaseDurationSeconds.toFloat()).coerceIn(0f, 1f)
    }
    
    fun getRepetitionDisplay(): String {
        return "${currentRepetitionIndex + 1}/$totalRepetitions"
    }
    
    fun getPhaseDisplay(): String {
        return "${currentPhaseIndex + 1}/$totalPhases"
    }
    
    fun isAtStart(): Boolean {
        return currentPhaseIndex == 0 && currentRepetitionIndex == 0
    }
    
    fun isAtEnd(): Boolean {
        return currentPhaseIndex >= totalPhases - 1 && 
               currentRepetitionIndex >= totalRepetitions - 1
    }
    
    companion object {
        val Idle = TimerState()
    }
}
