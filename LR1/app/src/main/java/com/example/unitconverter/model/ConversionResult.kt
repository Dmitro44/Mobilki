package com.example.unitconverter.model

/**
 * Represents the result of a unit conversion
 */
data class ConversionResult(
    val inputValue: Double,
    val outputValue: Double,
    val fromUnit: UnitItem,
    val toUnit: UnitItem,
    val formattedResult: String = ""
)
