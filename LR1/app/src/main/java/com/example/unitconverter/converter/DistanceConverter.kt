package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem

/**
 * Converter for distance/length units
 */
class DistanceConverter : Converter {
    
    companion object {
        const val CATEGORY_ID = "distance"
        
        // Conversion factors to meters (base unit)
        private val TO_METERS = mapOf(
            "m" to 1.0,
            "km" to 1000.0,
            "cm" to 0.01,
            "mm" to 0.001,
            "mi" to 1609.344,
            "yd" to 0.9144,
            "ft" to 0.3048,
            "in" to 0.0254
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
    
    override fun convert(value: Double, fromUnit: UnitItem, toUnit: UnitItem): Double {
        val fromFactor = TO_METERS[fromUnit.id] ?: 1.0
        val toFactor = TO_METERS[toUnit.id] ?: 1.0
        
        // Convert to meters first, then to target unit
        val valueInMeters = value * fromFactor
        return valueInMeters / toFactor
    }
}
