package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun OperationsPad(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryTextColor = MaterialTheme.colorScheme.onPrimary
    val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
    val secondaryTextColor = MaterialTheme.colorScheme.onSecondaryContainer

    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("C", Modifier.weight(1f), secondaryColor, secondaryTextColor, fontSize = 20.sp) { onButtonClick("C") }
            CalculatorButton("DEL", Modifier.weight(1f), secondaryColor, secondaryTextColor, fontSize = 20.sp) { onButtonClick("DEL") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("/", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("/") }
            CalculatorButton("*", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("*") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("-", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("-") }
            CalculatorButton("+", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("+") }
        }
    }
}
