package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem

/**
 * Converter for weight/mass units
 */
class WeightConverter : Converter {
    
    companion object {
        const val CATEGORY_ID = "weight"
        
        // Conversion factors to grams (base unit)
        private val TO_GRAMS = mapOf(
            "g" to 1.0,
            "kg" to 1000.0,
            "mg" to 0.001,
            "t" to 1_000_000.0,
            "lb" to 453.592,
            "oz" to 28.3495
        )
    }
    
    private val units = listOf(
        UnitItem("g", "Gram", "g", CATEGORY_ID),
        UnitItem("kg", "Kilogram", "kg", CATEGORY_ID),
        UnitItem("mg", "Milligram", "mg", CATEGORY_ID),
        UnitItem("t", "Tonne", "t", CATEGORY_ID),
        UnitItem("lb", "Pound", "lb", CATEGORY_ID),
        UnitItem("oz", "Ounce", "oz", CATEGORY_ID)
    )
    
    override fun getCategoryId(): String = CATEGORY_ID
    
    override fun getAvailableUnits(): List<UnitItem> = units
    
    override fun convert(value: Double, fromUnit: UnitItem, toUnit: UnitItem): Double {
        val fromFactor = TO_GRAMS[fromUnit.id] ?: 1.0
        val toFactor = TO_GRAMS[toUnit.id] ?: 1.0
        
        // Convert to grams first, then to target unit
        val valueInGrams = value * fromFactor
        return valueInGrams / toFactor
    }
}
