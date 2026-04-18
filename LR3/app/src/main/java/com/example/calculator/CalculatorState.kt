package com.example.calculator

data class CalculatorState(
    val expression: String = "",
    val result: String = "",
    val isEngineeringMode: Boolean = false
)
