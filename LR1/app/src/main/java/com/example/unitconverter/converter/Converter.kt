package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem
import java.math.BigDecimal

interface Converter {
    
    fun getCategoryId(): String
    
    fun getAvailableUnits(): List<UnitItem>
    
    fun convert(value: BigDecimal, fromUnit: UnitItem, toUnit: UnitItem): BigDecimal
}
