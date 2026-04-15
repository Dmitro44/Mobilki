package com.example.timer.domain.model

import android.content.Context
import com.example.timer.R

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

fun PhaseType.getLocalizedName(context: Context): String {
    return when (this) {
        PhaseType.WARMUP -> context.getString(R.string.phase_warmup)
        PhaseType.WORK -> context.getString(R.string.phase_work)
        PhaseType.REST -> context.getString(R.string.phase_rest)
        PhaseType.COOLDOWN -> context.getString(R.string.phase_cooldown)
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
