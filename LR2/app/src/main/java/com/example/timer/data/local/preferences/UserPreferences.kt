package com.example.timer.data.local.preferences

data class UserPreferences(
    val isDarkTheme: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val language: Language = Language.ENGLISH
)

enum class FontSize(val scale: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f);
    
    companion object {
        fun fromOrdinal(ordinal: Int): FontSize {
            return entries.getOrNull(ordinal) ?: MEDIUM
        }
    }
}

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский");
    
    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
        
        fun fromOrdinal(ordinal: Int): Language {
            return entries.getOrNull(ordinal) ?: ENGLISH
        }
    }
}
