package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem
import java.math.BigDecimal
import java.math.RoundingMode

class DistanceConverter : Converter {
    
    companion object {
        const val CATEGORY_ID = "distance"
        
        // Conversion factors to meters (base unit)
        private val TO_METERS = mapOf(
            "m" to BigDecimal("1.0"),
            "km" to BigDecimal("1000.0"),
            "cm" to BigDecimal("0.01"),
            "mm" to BigDecimal("0.001"),
            "mi" to BigDecimal("1609.344"),
            "yd" to BigDecimal("0.9144"),
            "ft" to BigDecimal("0.3048"),
            "in" to BigDecimal("0.0254")
        )
    }
    
    private val units = listOf(
        UnitItem("m", "Meter", "m", CATEGORY_ID),
        UnitItem("km", "Kilometer", "km", CATEGORY_ID),
        UnitItem("cm", "Centimeter", "cm", CATEGORY_ID),
        UnitItem("mm", "Millimeter", "mm", CATEGORY_ID),
        UnitItem("mi", "Mile", "mi", CATEGORY_ID),
        UnitItem("yd", "Yard", "yd", CATEGORY_ID),
        UnitItem("ft", "Foot", "ft", CATEGORY_ID),
        UnitItem("in", "Inch", "in", CATEGORY_ID)
    )
    
    override fun getCategoryId(): String = CATEGORY_ID
    
    override fun getAvailableUnits(): List<UnitItem> = units
    
    override fun convert(value: BigDecimal, fromUnit: UnitItem, toUnit: UnitItem): BigDecimal {
        val fromFactor = TO_METERS[fromUnit.id] ?: BigDecimal.ONE
        val toFactor = TO_METERS[toUnit.id] ?: BigDecimal.ONE
        
        // Convert to meters first, then to target unit
        val valueInMeters = value.multiply(fromFactor)
        return valueInMeters.divide(toFactor, 30, RoundingMode.HALF_UP)
    }
}
