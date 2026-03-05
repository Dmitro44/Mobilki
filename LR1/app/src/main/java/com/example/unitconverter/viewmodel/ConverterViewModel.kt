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
import kotlin.math.pow

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
     *
     * The authoritative source of truth for the current numeric value is ALWAYS
     * _conversionResult (which stores raw Doubles), never the formatted strings in
     * _inputValue / _outputValue.  Formatted strings can silently lose precision for
     * very small numbers (e.g. 1e-11 formats to "0" with fixed-decimal rounding), so
     * reading them back would corrupt the conversion chain.
     */
    fun swapUnits() {
        val tempFrom = _fromUnit.value
        val tempTo   = _toUnit.value

        // ── Step 1: Capture the precise numeric state BEFORE touching anything ────
        //
        // _conversionResult always holds the full-precision Double values produced by
        // the last successful conversion.  We must read it here, before we reassign
        // _fromUnit / _toUnit (which would trigger observers that might clear it).
        val conv = _conversionResult.value

        // After the swap:
        //   • the new "input" should be what was previously the OUTPUT value
        //     (so the user is now converting in the opposite direction from the same quantity)
        //   • the new "output" is whatever the converter returns for that input
        //
        // conv.outputValue is the precise Double for the current output field.
        // conv.inputValue  is the precise Double for the current input field.
        // We prefer outputValue; fall back to inputValue if no result exists yet.
        val newInputValue: Double? = conv?.outputValue ?: conv?.inputValue

        // ── Step 2: Swap the unit selections ──────────────────────────────────────
        _fromUnit.value = tempTo
        _toUnit.value   = tempFrom

        // ── Step 3: Handle the case where there is no value to convert ────────────
        if (newInputValue == null) {
            _inputValue.value        = ""
            _outputValue.value       = ""
            _conversionResult.value  = null
            return
        }

        // ── Step 4: Run a fresh conversion with the swapped units ─────────────────
        val category  = _selectedCategory.value
        val converter = category?.let { converters[it.id] }
        if (converter == null) {
            _error.value = "Converter not found"
            return
        }

        // Format and publish the new input (the old precise output value)
        _inputValue.value = formatNumber(newInputValue)

        try {
            val result = converter.convert(newInputValue, _fromUnit.value!!, _toUnit.value!!)
            _outputValue.value = formatNumber(result)

            _conversionResult.value = ConversionResult(
                inputValue      = newInputValue,
                outputValue     = result,
                fromUnit        = _fromUnit.value!!,
                toUnit          = _toUnit.value!!,
                formattedResult = "${formatNumber(result)} ${_toUnit.value!!.symbol}"
            )
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Conversion error: ${e.message}"
        }
    }

    // Helper to format a Double as display string (no unit symbol).
    // Uses significant-figure rounding so that very small numbers like 1e-11
    // are never silently collapsed to 0 by fixed-decimal rounding.
    private fun formatNumber(value: Double): String {
        if (value == 0.0) return "0"

        val abs = kotlin.math.abs(value)

        // Round to 10 *significant* figures instead of 10 *decimal places*.
        // e.g. 1.0011e-11 → magnitude = -11, so we keep digits at 1e-21 scale.
        val magnitude = kotlin.math.floor(kotlin.math.log10(abs)).toInt()
        val scale = 10.0.pow((10 - 1 - magnitude).toDouble())
        val rounded = kotlin.math.round(value * scale) / scale

        if (rounded == 0.0) return "0"

        // Whole numbers: show without decimal point
        if (rounded == rounded.toLong().toDouble() && abs >= 1.0) {
            return rounded.toLong().toString()
        }

        // For very small or very large numbers outside a readable range, use enough
        // decimal places to show 10 significant figures.
        val decimalPlaces = maxOf(0, 10 - 1 - magnitude)
        return String.format("%.${decimalPlaces}f", rounded).trimEnd('0').trimEnd('.')
    }
    
    // Helper to format user input (preserves trailing zeros and decimal point)
    private fun formatUserInput(value: String): String {
        return value  // Return as-is to allow "0." input
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

        val outputDouble = output.toDoubleOrNull()
        if (outputDouble == null) {
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
            val result = converter.convert(outputDouble, to, from)
            val formatted = formatResult(result, from)

            _inputValue.value = formatNumber(result)

            _conversionResult.value = ConversionResult(
                inputValue = result,
                outputValue = outputDouble,
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

        val inputDouble = input.toDoubleOrNull()
        if (inputDouble == null) {
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
            val result = converter.convert(inputDouble, from, to)
            val formatted = formatResult(result, to)

            _conversionResult.value = ConversionResult(
                inputValue = inputDouble,
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
    private fun formatResult(value: Double, unit: UnitItem): String {
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
     * Parses the string to Double safely and performs forward conversion.
     * Ignores invalid (non-numeric) input by setting empty value.
     */
    fun setInputValueString(value: String) {
        val trimmed = value.trim()

        if (trimmed.isEmpty()) {
            _inputValue.value = ""
            _outputValue.value = ""
            _conversionResult.value = null
            return
        }

        // Try to parse as Double, ignore if invalid
        val parsed = trimmed.toDoubleOrNull()
        if (parsed == null) {
            // Invalid input - ignore or reset
            _inputValue.value = ""
            _outputValue.value = ""
            _conversionResult.value = null
            return
        }

        // Keep user input as-is (preserve trailing zeros and decimal point)
        _inputValue.value = trimmed
        performConversion()
    }

    /**
     * Sets the output value from a string (for paste/direct input).
     * Parses the string to Double safely and performs reverse conversion.
     * Ignores invalid (non-numeric) input by setting empty value.
     */
    fun setOutputValueString(value: String) {
        val trimmed = value.trim()

        if (trimmed.isEmpty()) {
            _outputValue.value = ""
            _inputValue.value = ""
            _conversionResult.value = null
            return
        }

        // Try to parse as Double, ignore if invalid
        val parsed = trimmed.toDoubleOrNull()
        if (parsed == null) {
            // Invalid input - ignore or reset
            _outputValue.value = ""
            _inputValue.value = ""
            _conversionResult.value = null
            return
        }

        // Keep user input as-is (preserve trailing zeros and decimal point)
        _outputValue.value = trimmed
        performReverseConversion()
    }
}
