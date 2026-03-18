package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem
import java.math.BigDecimal

/**
 * Base interface for all converters
 */
interface Converter {
    
    /**
     * Returns the category ID this converter handles
     */
    fun getCategoryId(): String
    
    /**
     * Returns list of available units for this converter
     */
    fun getAvailableUnits(): List<UnitItem>
    
    /**
     * Converts a value from one unit to another
     * @param value the value to convert
     * @param fromUnit the source unit
     * @param toUnit the target unit
     * @return the converted value
     */
    fun convert(value: BigDecimal, fromUnit: UnitItem, toUnit: UnitItem): BigDecimal
}
