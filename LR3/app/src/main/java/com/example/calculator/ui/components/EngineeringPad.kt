package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EngineeringPad(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
    val secondaryTextColor = MaterialTheme.colorScheme.onSecondaryContainer

    if (isLandscape) {
        Column(modifier = modifier) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CalculatorButton("!", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("!") }
                CalculatorButton("^", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("^") }
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CalculatorButton("√", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("√") }
                CalculatorButton("%", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("%") }
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CalculatorButton("(", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("(") }
                CalculatorButton(")", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick(")") }
            }
        }
    } else {
        Row(modifier = modifier.fillMaxWidth()) {
            CalculatorButton("!", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("!") }
            CalculatorButton("^", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("^") }
            CalculatorButton("√", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("√") }
            CalculatorButton("(", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("(") }
            CalculatorButton(")", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick(")") }
        }
    }
}
