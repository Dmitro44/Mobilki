package com.example.unitconverter.model

/**
 * Represents a unit of measurement within a category
 */
data class UnitItem(
    val id: String,
    val name: String,
    val symbol: String,
    val categoryId: String
)
