package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calculator.ui.theme.CalculatorTheme

@Composable
fun CalculatorDisplay(
    expression: String,
    result: String,
    modifier: Modifier = Modifier
) {
    val expressionScrollState = rememberScrollState()
    val resultScrollState = rememberScrollState()

    LaunchedEffect(expression) {
        expressionScrollState.animateScrollTo(expressionScrollState.maxValue)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(expressionScrollState)
            )
            Text(
                text = result,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(resultScrollState)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorDisplayPreview() {
    CalculatorTheme {
        CalculatorDisplay(
            expression = "123 + 456",
            result = "579"
        )
    }
}
