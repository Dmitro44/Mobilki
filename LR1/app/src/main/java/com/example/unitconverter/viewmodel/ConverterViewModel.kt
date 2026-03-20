package com.example.unitconverter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.unitconverter.converter.Converter
import com.example.unitconverter.converter.CurrencyConverter
import com.example.unitconverter.converter.DistanceConverter
import com.example.unitconverter.converter.WeightConverter
import com.example.unitconverter.model.Category
import com.example.unitconverter.model.ConversionResult
import com.example.unitconverter.model.UnitItem
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * ViewModel for the Unit Converter application.
 * Manages conversion state and logic, shared between fragments.
 */
class ConverterViewModel : ViewModel() {

    // Map of category ID to converter implementation
    private val converters: Map<String, Converter> = mapOf(
        DistanceConverter.CATEGORY_ID to DistanceConverter(),
        WeightConverter.CATEGORY_ID to WeightConverter(),
        CurrencyConverter.CATEGORY_ID to CurrencyConverter()
    )

    // Available categories
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // Currently selected category
    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    // Available units for selected category
    private val _availableUnits = MutableLiveData<List<UnitItem>>()
    val availableUnits: LiveData<List<UnitItem>> = _availableUnits

    // Selected source unit
    private val _fromUnit = MutableLiveData<UnitItem?>()
    val fromUnit: LiveData<UnitItem?> = _fromUnit

    // Selected target unit
    private val _toUnit = MutableLiveData<UnitItem?>()
    val toUnit: LiveData<UnitItem?> = _toUnit

    // Input value as string (for display)
    private val _inputValue = MutableLiveData<String>()
    val inputValue: LiveData<String> = _inputValue

    // Conversion result
    private val _conversionResult = MutableLiveData<ConversionResult?>()
    val conversionResult: LiveData<ConversionResult?> = _conversionResult

    // Error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Active input field enum
    enum class ActiveField { INPUT, OUTPUT }

    // Currently active input field
    private val _activeField = MutableLiveData<ActiveField>()
    val activeField: LiveData<ActiveField> = _activeField

    // Output value (for bidirectional conversion)
    private val _outputValue = MutableLiveData<String>()
    val outputValue: LiveData<String> = _outputValue

    init {
        // Initialize categories
        _categories.value = listOf(
            Category(DistanceConverter.CATEGORY_ID, "Distance"),
            Category(WeightConverter.CATEGORY_ID, "Weight"),
            Category(CurrencyConverter.CATEGORY_ID, "Currency")
        )

        _inputValue.value = ""
        _outputValue.value = ""
        _activeField.value = ActiveField.INPUT
    }

    /**
     * Sets the currently active input field (INPUT or OUTPUT)
     */
    fun setActiveField(field: ActiveField) {
        _activeField.value = field
    }

    /**
     * Called when a digit button is pressed on the keyboard
     */
    fun onDigitClick(digit: String) {
        when (_activeField.value) {
            ActiveField.OUTPUT -> appendToOutput(digit)
            else -> appendToInput(digit)
        }
    }

    /**
     * Called when the clear button is pressed
     */
    fun onClearClick() {
        when (_activeField.value) {
            ActiveField.OUTPUT -> {
                _outputValue.value = ""
                _inputValue.value = ""
                _conversionResult.value = null
            }

            else -> clearInput()
        }
    }

    /**
     * Called when the delete/backspace button is pressed
     */
    fun onDeleteClick() {
        when (_activeField.value) {
            ActiveField.OUTPUT -> deleteLastCharFromOutput()
            else -> deleteLastChar()
        }
    }

    /**
     * Selects a category and updates available units
     */
    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        val converter = converters[category.id]
        val units = converter?.getAvailableUnits() ?: emptyList()
        _availableUnits.value = units

        // Reset unit selection
        if (units.isNotEmpty()) {
            _fromUnit.value = units.first()
            _toUnit.value = if (units.size > 1) units[1] else units.first()
        } else {
            _fromUnit.value = null
            _toUnit.value = null
        }

        // Reset result
        _conversionResult.value = null
        _error.value = null

        _inputValue.value = ""
    }

    /**
     * Sets the source unit for conversion
     */
    fun setFromUnit(unit: UnitItem) {
        _fromUnit.value = unit
        performConversion()
    }

    /**
     * Sets the target unit for conversion
     */
    fun setToUnit(unit: UnitItem) {
        _toUnit.value = unit
        performConversion()
    }

    /**
     * Swaps source and target units.
     */
    fun swapUnits() {
        val tempFrom = _fromUnit.value
        val tempTo = _toUnit.value

        val conv = _conversionResult.value
        val newInputValue: BigDecimal? = conv?.outputValue ?: conv?.inputValue

        _fromUnit.value = tempTo
        _toUnit.value = tempFrom

        if (newInputValue == null) {
            _inputValue.value = ""
            _outputValue.value = ""
            _conversionResult.value = null
            return
        }

        val category = _selectedCategory.value
        val converter = category?.let { converters[it.id] }
        if (converter == null) {
            _error.value = "Converter not found"
            return
        }

        // Format the number for editing to ensure users can append digits without issues
        _inputValue.value = formatNumberForEditing(newInputValue)

        try {
            val result = converter.convert(newInputValue, _fromUnit.value!!, _toUnit.value!!)
            _outputValue.value = formatNumberForEditing(result)

            _conversionResult.value = ConversionResult(
                inputValue = newInputValue,
                outputValue = result,
                fromUnit = _fromUnit.value!!,
                toUnit = _toUnit.value!!,
                formattedResult = "${formatNumber(result)} ${_toUnit.value!!.symbol}"
            )
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Conversion error: ${e.message}"
        }
    }

    // Formats a BigDecimal for display without a unit symbol.
    // Uses toPlainString() to avoid scientific notation.
    private fun formatNumber(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        
        // Strip trailing zeros and use plain string (no scientific notation)
        return value.stripTrailingZeros().toPlainString()
    }

    /**
     * Formats a number for editing (user input).
     * This ensures the output is always in decimal notation (toPlainString).
     */
    private fun formatNumberForEditing(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"

        // Limit to 30 decimal places for safety, then strip trailing zeros
        return value.setScale(30, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    /**
     * Called when swap button is pressed (premium feature)
     */
    fun onSwapClick() {
        swapUnits()
    }

    /**
     * Updates the input value from keyboard input
     */
    fun updateInputValue(value: String) {
        _inputValue.value = value
        performConversion()
    }

    /**
     * Appends a digit or decimal point to the input
     */
    fun appendToInput(char: String) {
        val current = _inputValue.value ?: ""

        // Prevent multiple decimal points
        if (char == "." && current.contains(".")) {
            return
        }

        // Prevent leading zeros (except for "0.")
        if (current == "0" && char != ".") {
            _inputValue.value = char
        } else {
            _inputValue.value = current + char
        }

        performConversion()
    }

    /**
     * Removes the last character from input
     */
    fun deleteLastChar() {
        val current = _inputValue.value ?: ""
        if (current.isNotEmpty()) {
            _inputValue.value = current.dropLast(1)
            performConversion()
        }
    }

    /**
     * Clears the input value
     */
    fun clearInput() {
        _inputValue.value = ""
        _outputValue.value = ""
        _conversionResult.value = null
        _error.value = null
    }

    /**
     * Appends a digit or decimal point to the output (for reverse conversion)
     */
    fun appendToOutput(char: String) {
        val current = _outputValue.value ?: ""

        // Prevent multiple decimal points
        if (char == "." && current.contains(".")) {
            return
        }

        // Prevent leading zeros (except for "0.")
        if (current == "0" && char != ".") {
            _outputValue.value = char
        } else {
            _outputValue.value = current + char
        }

        performReverseConversion()
    }

    /**
     * Removes the last character from output
     */
    fun deleteLastCharFromOutput() {
        val current = _outputValue.value ?: ""
        if (current.isNotEmpty()) {
            _outputValue.value = current.dropLast(1)
            performReverseConversion()
        }
    }

    /**
     * Performs reverse conversion (from output to input)
     */
    private fun performReverseConversion() {
        val category = _selectedCategory.value ?: return
        val from = _fromUnit.value ?: return
        val to = _toUnit.value ?: return
        val output = _outputValue.value ?: ""

        if (output.isEmpty() || output == ".") {
            _inputValue.value = ""
            _conversionResult.value = null
            return
        }

        // Input validation: limit length to prevent performance issues
        if (output.length > 500) {
            _error.value = "Input too long"
            return
        }

        val outputBigDecimal = try { output.toBigDecimal() } catch (e: Exception) { null }
        if (outputBigDecimal == null) {
            _error.value = "Invalid input"
            return
        }

        val converter = converters[category.id]
        if (converter == null) {
            _error.value = "Converter not found"
            return
        }

        try {
            // Reverse conversion: from toUnit to fromUnit
            val result = converter.convert(outputBigDecimal, to, from)
            val formatted = formatResult(result, from)

            _inputValue.value = formatNumber(result)

            _conversionResult.value = ConversionResult(
                inputValue = result,
                outputValue = outputBigDecimal,
                fromUnit = from,
                toUnit = to,
                formattedResult = "$output ${to.symbol}"
            )
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Conversion error: ${e.message}"
        }
    }

    /**
     * Performs the conversion based on current state
     */
    private fun performConversion() {
        val category = _selectedCategory.value ?: return
        val from = _fromUnit.value ?: return
        val to = _toUnit.value ?: return
        val input = _inputValue.value ?: ""

        if (input.isEmpty() || input == ".") {
            _conversionResult.value = null
            _outputValue.value = ""
            return
        }

        if (input.length > 500) {
            _error.value = "Input too long"
            return
        }

        val inputBigDecimal = try { input.toBigDecimal() } catch (e: Exception) { null }
        if (inputBigDecimal == null) {
            _error.value = "Invalid input"
            _conversionResult.value = null
            _outputValue.value = ""
            return
        }

        val converter = converters[category.id]
        if (converter == null) {
            _error.value = "Converter not found"
            _conversionResult.value = null
            return
        }

        try {
            val result = converter.convert(inputBigDecimal, from, to)
            val formatted = formatResult(result, to)

            _conversionResult.value = ConversionResult(
                inputValue = inputBigDecimal,
                outputValue = result,
                fromUnit = from,
                toUnit = to,
                formattedResult = formatted
            )
            _outputValue.value = formatNumber(result)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Conversion error: ${e.message}"
            _conversionResult.value = null
        }
    }

    /**
     * Formats the result for display (includes unit symbol).
     * Delegates number formatting to formatNumber() so precision is consistent.
     */
    private fun formatResult(value: BigDecimal, unit: UnitItem): String {
        return "${formatNumber(value)} ${unit.symbol}"
    }

    /**
     * Gets the converter for a specific category
     */
    fun getConverter(categoryId: String): Converter? {
        return converters[categoryId]
    }

    /**
     * Sets the input value from a string (for paste/direct input).
     */
    fun setInputValueString(value: String) {
        val trimmed = value.trim()

        if (trimmed.isEmpty()) {
            _inputValue.value = ""
            _outputValue.value = ""
            _conversionResult.value = null
            return
        }

        if (trimmed.length > 500) {
            _error.value = "Input too long"
            return
        }

        val parsed = try { trimmed.toBigDecimal() } catch (e: Exception) { null }
        if (parsed == null) {
            _inputValue.value = ""
            _outputValue.value = ""
            _conversionResult.value = null
            return
        }

        _inputValue.value = trimmed
        performConversion()
    }

    /**
     * Sets the output value from a string (for paste/direct input).
     */
    fun setOutputValueString(value: String) {
        val trimmed = value.trim()

        if (trimmed.isEmpty()) {
            _outputValue.value = ""
            _inputValue.value = ""
            _conversionResult.value = null
            return
        }

        if (trimmed.length > 500) {
            _error.value = "Input too long"
            return
        }

        val parsed = try { trimmed.toBigDecimal() } catch (e: Exception) { null }
        if (parsed == null) {
            _outputValue.value = ""
            _inputValue.value = ""
            _conversionResult.value = null
            return
        }

        _outputValue.value = trimmed
        performReverseConversion()
    }
}
