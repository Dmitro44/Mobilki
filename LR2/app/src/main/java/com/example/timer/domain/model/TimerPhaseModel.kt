package com.example.timer.domain.model

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

data class TimerPhaseModel(
    val id: Long = 0,
    val sequenceId: Long,
    val phaseType: PhaseType,
    val durationSeconds: Int,
    val repetitions: Int = 1,
    val order: Int
) {
    val totalDurationSeconds: Int
        get() = durationSeconds * repetitions
    
    fun isValid(): Boolean {
        return durationSeconds > 0 && repetitions > 0 && order >= 0
    }
}
