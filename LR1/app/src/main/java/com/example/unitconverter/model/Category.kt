package com.example.unitconverter.model

/**
 * Represents a conversion category (e.g., Distance, Weight, Currency)
 */
data class Category(
    val id: String,
    val name: String,
    val iconResId: Int = 0
)
