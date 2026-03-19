package com.example.timer.domain.model

/**
 * Enum representing the type of phase in a timer sequence
 */
enum class PhaseType {
    WARMUP,
    WORK,
    REST,
    COOLDOWN;
    
    companion object {
        fun fromString(value: String): PhaseType {
            return entries.find { it.name.equals(value, ignoreCase = true) } 
                ?: WORK
        }
    }
}

/**
 * Domain model for a timer phase
 * Decouples business logic from database entities
 */
data class TimerPhaseModel(
    val id: Long = 0,
    val sequenceId: Long,
    val phaseType: PhaseType,
    val durationSeconds: Int,
    val repetitions: Int = 1,
    val order: Int
) {
    /**
     * Calculate total duration including all repetitions
     */
    val totalDurationSeconds: Int
        get() = durationSeconds * repetitions
    
    /**
     * Validate phase data
     */
    fun isValid(): Boolean {
        return durationSeconds > 0 && repetitions > 0 && order >= 0
    }
}
