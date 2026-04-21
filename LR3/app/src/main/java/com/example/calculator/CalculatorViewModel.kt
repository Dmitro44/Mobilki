package com.example.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state = _state.asStateFlow()

    private val engine = CalculatorEngine()

    fun onAction(action: CalculatorAction) {
        if (_state.value.expression == "Error" && action !is CalculatorAction.Clear) {
            _state.update { it.copy(expression = "", result = "") }
        }
        if (_state.value.result.isNotEmpty() && action !is CalculatorAction.Calculate && action !is CalculatorAction.Clear && action !is CalculatorAction.ToggleMode) {
            _state.update { it.copy(result = "") }
        }
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> _state.update { it.copy(expression = "", result = "") }
            is CalculatorAction.Delete -> _state.update { it.copy(expression = it.expression.dropLast(1), result = "") }
            is CalculatorAction.Calculate -> calculate()
            is CalculatorAction.ToggleMode -> _state.update { it.copy(isEngineeringMode = !it.isEngineeringMode) }
        }
    }

    private fun enterNumber(number: Int) {
        _state.update { state ->
            val expr = state.expression
            if (expr.length >= 200) return@update state
            state.copy(expression = expr + number)
        }
    }

    private fun enterOperation(operation: String) {
        _state.update { state ->
            val expr = state.expression
            val op = if (operation == "sqrt") "√" else operation

            if (expr.isEmpty()) {
                if (op == "-" || op == "√" || op == "(") {
                    return@update state.copy(expression = op)
                }
                return@update state
            }

            val lastChar = expr.last()

            if (op == "√") {
                return@update if (lastChar.isDigit() || lastChar == ')' || lastChar == '.') {
                    state.copy(expression = "√($expr)")
                } else {
                    state.copy(expression = expr + op)
                }
            }

            if (lastChar in setOf('+', '-', '*', '/', '^') && op in setOf("+", "-", "*", "/", "^")) {
                return@update state.copy(expression = expr.dropLast(1) + op)
            }

            state.copy(expression = expr + op)
        }
    }

    private fun enterDecimal() {
        _state.update { state ->
            val expr = state.expression
            if (expr.isEmpty()) return@update state.copy(expression = "0.")

            val lastChar = expr.last()
            if (!lastChar.isDigit() && lastChar != '.') {
                return@update state.copy(expression = expr + "0.")
            }

            val lastOperatorIndex = expr.indexOfLast { !it.isDigit() && it != '.' }
            val currentNumber = if (lastOperatorIndex == -1) expr else expr.substring(lastOperatorIndex + 1)

            if (!currentNumber.contains(".")) {
                state.copy(expression = expr + ".")
            } else {
                state
            }
        }
    }

    private fun calculate() {
        _state.update { state ->
            val result = engine.evaluate(state.expression)
            state.copy(result = result)
        }
    }
}
