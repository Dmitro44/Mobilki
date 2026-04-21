package com.example.calculator.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    fontSize: TextUnit = 24.sp,
    onClick: () -> Unit
) {
    val bgColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.surfaceVariant else color
    val contentColor = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else textColor

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor
        )
    ) {
        Text(
            fontSize = fontSize,
            text = symbol,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
