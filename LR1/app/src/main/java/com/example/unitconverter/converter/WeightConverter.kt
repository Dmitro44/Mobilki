package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem
import java.math.BigDecimal
import java.math.RoundingMode

class WeightConverter : Converter {
    
    companion object {
        const val CATEGORY_ID = "weight"
        
        private val TO_GRAMS = mapOf(
            "g" to BigDecimal("1.0"),
            "kg" to BigDecimal("1000.0"),
            "mg" to BigDecimal("0.001"),
            "t" to BigDecimal("1000000.0"),
            "lb" to BigDecimal("453.592"),
            "oz" to BigDecimal("28.3495")
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
    
    override fun convert(value: BigDecimal, fromUnit: UnitItem, toUnit: UnitItem): BigDecimal {
        val fromFactor = TO_GRAMS[fromUnit.id] ?: BigDecimal.ONE
        val toFactor = TO_GRAMS[toUnit.id] ?: BigDecimal.ONE
        
        // Convert to grams first, then to target unit
        val valueInGrams = value.multiply(fromFactor)
        return valueInGrams.divide(toFactor, 30, RoundingMode.HALF_UP)
    }
}
