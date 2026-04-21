package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NumberPad(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val surfaceTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryTextColor = MaterialTheme.colorScheme.onPrimary

    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("7", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("7") }
            CalculatorButton("8", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("8") }
            CalculatorButton("9", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("9") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("4", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("4") }
            CalculatorButton("5", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("5") }
            CalculatorButton("6", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("6") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("1", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("1") }
            CalculatorButton("2", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("2") }
            CalculatorButton("3", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("3") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("0", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("0") }
            CalculatorButton(".", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick(".") }
            CalculatorButton("=", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("=") }
        }
    }
}
