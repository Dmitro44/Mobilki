package com.example.timer.data.local.converter

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter

/**
 * Type converter for Room to handle Compose Color objects
 */
class ColorConverter {
    @TypeConverter
    fun fromColor(color: Color): Int {
        return color.toArgb()
    }

    @TypeConverter
    fun toColor(colorInt: Int): Color {
        return Color(colorInt)
    }
}
