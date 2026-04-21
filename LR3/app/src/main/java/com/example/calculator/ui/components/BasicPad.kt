package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme

@Composable
fun BasicPad(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryTextColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val surfaceTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
    val secondaryTextColor = MaterialTheme.colorScheme.onSecondaryContainer

    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton(
                symbol = "C",
                modifier = Modifier.weight(1f),
                color = secondaryColor,
                textColor = secondaryTextColor,
                fontSize = 20.sp,
                onClick = { onButtonClick("C") }
            )
            CalculatorButton(
                symbol = "DEL",
                modifier = Modifier.weight(1f),
                color = secondaryColor,
                textColor = secondaryTextColor,
                fontSize = 20.sp,
                onClick = { onButtonClick("DEL") }
            )
            CalculatorButton(
                symbol = "%",
                modifier = Modifier.weight(1f),
                color = secondaryColor,
                textColor = secondaryTextColor,
                onClick = { onButtonClick("%") }
            )
            CalculatorButton(
                symbol = "/",
                modifier = Modifier.weight(1f),
                color = primaryColor,
                textColor = primaryTextColor,
                onClick = { onButtonClick("/") }
            )
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("7", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("7") }
            CalculatorButton("8", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("8") }
            CalculatorButton("9", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("9") }
            CalculatorButton("*", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("*") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("4", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("4") }
            CalculatorButton("5", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("5") }
            CalculatorButton("6", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("6") }
            CalculatorButton("-", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("-") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("1", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("1") }
            CalculatorButton("2", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("2") }
            CalculatorButton("3", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick("3") }
            CalculatorButton("+", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("+") }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CalculatorButton("0", Modifier.weight(2f), surfaceColor, surfaceTextColor) { onButtonClick("0") }
            CalculatorButton(".", Modifier.weight(1f), surfaceColor, surfaceTextColor) { onButtonClick(".") }
            CalculatorButton("=", Modifier.weight(1f), primaryColor, primaryTextColor) { onButtonClick("=") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BasicPadPreview() {
    CalculatorTheme {
        BasicPad(
            onButtonClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
