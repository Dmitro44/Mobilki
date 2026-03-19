package com.example.timer.utils

/**
 * Extension functions for formatting time values
 */

/**
 * Format seconds as a time string (MM:SS or HH:MM:SS)
 * @return Formatted time string
 */
fun Int.toTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Format seconds as a compact time string without leading zeros (M:SS or H:MM:SS)
 * @return Compact formatted time string
 */
fun Int.toCompactTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
