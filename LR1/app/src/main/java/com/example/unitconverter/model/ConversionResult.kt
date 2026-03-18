package com.example.unitconverter.model

import java.math.BigDecimal

/**
 * Represents the result of a unit conversion
 */
data class ConversionResult(
    val inputValue: BigDecimal,
    val outputValue: BigDecimal,
    val fromUnit: UnitItem,
    val toUnit: UnitItem,
    val formattedResult: String = ""
)
