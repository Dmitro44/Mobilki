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

class ConverterViewModel : ViewModel() {

    private val converters: Map<String, Converter> = mapOf(
        DistanceConverter.CATEGORY_ID to DistanceConverter(),
        WeightConverter.CATEGORY_ID to WeightConverter(),
        CurrencyConverter.CATEGORY_ID to CurrencyConverter()
    )

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _availableUnits = MutableLiveData<List<UnitItem>>()
    val availableUnits: LiveData<List<UnitItem>> = _availableUnits

    private val _fromUnit = MutableLiveData<UnitItem?>()
    val fromUnit: LiveData<UnitItem?> = _fromUnit

    private val _toUnit = MutableLiveData<UnitItem?>()
    val toUnit: LiveData<UnitItem?> = _toUnit

    private val _inputValue = MutableLiveData<String>()
    val inputValue: LiveData<String> = _inputValue

    private val _conversionResult = MutableLiveData<ConversionResult?>()
    val conversionResult: LiveData<ConversionResult?> = _conversionResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    enum class ActiveField { INPUT, OUTPUT }

    private val _activeField = MutableLiveData<ActiveField>()
    val activeField: LiveData<ActiveField> = _activeField

    private val _outputValue = MutableLiveData<String>()
    val outputValue: LiveData<String> = _outputValue

    init {
        _categories.value = listOf(
            Category(DistanceConverter.CATEGORY_ID, "Distance"),
            Category(WeightConverter.CATEGORY_ID, "Weight"),
            Category(CurrencyConverter.CATEGORY_ID, "Currency")
        )

        _inputValue.value = ""
        _outputValue.value = ""
        _activeField.value = ActiveField.INPUT
    }

    fun setActiveField(field: ActiveField) {
        _activeField.value = field
    }

    fun onDigitClick(digit: String) {
        when (_activeField.value) {
            ActiveField.OUTPUT -> appendToOutput(digit)
            else -> appendToInput(digit)
        }
    }

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

    fun onDeleteClick() {
        when (_activeField.value) {
            ActiveField.OUTPUT -> deleteLastCharFromOutput()
            else -> deleteLastChar()
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        val converter = converters[category.id]
        val units = converter?.getAvailableUnits() ?: emptyList()
        _availableUnits.value = units

        if (units.isNotEmpty()) {
            _fromUnit.value = units.first()
            _toUnit.value = if (units.size > 1) units[1] else units.first()
        } else {
            _fromUnit.value = null
            _toUnit.value = null
        }

        _conversionResult.value = null
        _error.value = null

        _inputValue.value = ""
    }

    fun setFromUnit(unit: UnitItem) {
        _fromUnit.value = unit
        performConversion()
    }

    fun setToUnit(unit: UnitItem) {
        _toUnit.value = unit
        performConversion()
    }

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
    private fun formatNumber(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        
        // Strip trailing zeros and use plain string (no scientific notation)
        return value.stripTrailingZeros().toPlainString()
    }

    private fun formatNumberForEditing(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"

        return value.setScale(30, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    fun onSwapClick() {
        swapUnits()
    }

    fun updateInputValue(value: String) {
        _inputValue.value = value
        performConversion()
    }

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

    fun deleteLastChar() {
        val current = _inputValue.value ?: ""
        if (current.isNotEmpty()) {
            _inputValue.value = current.dropLast(1)
            performConversion()
        }
    }

    fun clearInput() {
        _inputValue.value = ""
        _outputValue.value = ""
        _conversionResult.value = null
        _error.value = null
    }

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

    fun deleteLastCharFromOutput() {
        val current = _outputValue.value ?: ""
        if (current.isNotEmpty()) {
            _outputValue.value = current.dropLast(1)
            performReverseConversion()
        }
    }

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

    private fun formatResult(value: BigDecimal, unit: UnitItem): String {
        return "${formatNumber(value)} ${unit.symbol}"
    }

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
}
