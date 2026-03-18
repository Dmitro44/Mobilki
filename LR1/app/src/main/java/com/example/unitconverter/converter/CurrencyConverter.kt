package com.example.unitconverter.converter

import com.example.unitconverter.model.UnitItem
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converter for currencies with static (mock) exchange rates.
 * No network calls - uses hardcoded rates for demonstration.
 */
class CurrencyConverter : Converter {
    
    companion object {
        const val CATEGORY_ID = "currency"
        
        // Static exchange rates to USD (base currency)
        // These are mock rates for demonstration purposes
        private val TO_USD = mapOf(
            "USD" to BigDecimal("1.0"),
            "EUR" to BigDecimal("1.08"),      // 1 EUR = 1.08 USD
            "GBP" to BigDecimal("1.27"),      // 1 GBP = 1.27 USD
            "RUB" to BigDecimal("0.011"),     // 1 RUB = 0.011 USD
            "UAH" to BigDecimal("0.027"),     // 1 UAH = 0.027 USD
            "BYN" to BigDecimal("0.31")       // 1 BYN = 0.31 USD
        )
    }
    
    private val units = listOf(
        UnitItem("USD", "US Dollar", "$", CATEGORY_ID),
        UnitItem("EUR", "Euro", "\u20AC", CATEGORY_ID),
        UnitItem("GBP", "British Pound", "\u00A3", CATEGORY_ID),
        UnitItem("RUB", "Russian Ruble", "\u20BD", CATEGORY_ID),
        UnitItem("UAH", "Ukrainian Hryvnia", "\u20B4", CATEGORY_ID),
        UnitItem("BYN", "Belarusian Ruble", "Br", CATEGORY_ID)
    )
    
    override fun getCategoryId(): String = CATEGORY_ID
    
    override fun getAvailableUnits(): List<UnitItem> = units
    
    override fun convert(value: BigDecimal, fromUnit: UnitItem, toUnit: UnitItem): BigDecimal {
        val fromRate = TO_USD[fromUnit.id] ?: BigDecimal.ONE
        val toRate = TO_USD[toUnit.id] ?: BigDecimal.ONE
        
        // Convert to USD first, then to target currency
        val valueInUsd = value.multiply(fromRate)
        return valueInUsd.divide(toRate, 30, RoundingMode.HALF_UP)
    }
}
