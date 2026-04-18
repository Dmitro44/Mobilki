package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.calculator.ui.theme.CalculatorTheme

@Composable
fun EngineeringPad(
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
    val secondaryTextColor = MaterialTheme.colorScheme.onSecondaryContainer

    Row(modifier = modifier.fillMaxWidth()) {
        CalculatorButton("!", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("!") }
        CalculatorButton("^", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("^") }
        CalculatorButton("√", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("√") }
        CalculatorButton("(", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick("(") }
        CalculatorButton(")", Modifier.weight(1f), secondaryColor, secondaryTextColor) { onButtonClick(")") }
    }
}

@Preview(showBackground = true)
@Composable
fun EngineeringPadPreview() {
    CalculatorTheme {
        EngineeringPad(
            onButtonClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
