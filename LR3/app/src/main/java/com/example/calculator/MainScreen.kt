package com.example.calculator

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import com.example.calculator.ui.components.BasicPad
import com.example.calculator.ui.components.CalculatorDisplay
import com.example.calculator.ui.components.EngineeringPad
import com.example.calculator.ui.components.NumberPad
import com.example.calculator.ui.components.OperationsPad

@Composable
fun MainScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isFullFlavor = BuildConfig.FLAVOR == "full"

    val onAction: (String) -> Unit = { symbol ->
        when (symbol) {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                viewModel.onAction(CalculatorAction.Number(symbol.toInt()))
            }
            "C" -> viewModel.onAction(CalculatorAction.Clear)
            "DEL" -> viewModel.onAction(CalculatorAction.Delete)
            "." -> viewModel.onAction(CalculatorAction.Decimal)
            "=" -> viewModel.onAction(CalculatorAction.Calculate)
            else -> viewModel.onAction(CalculatorAction.Operation(symbol))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CalculatorDisplay(
            expression = state.expression,
            result = state.result,
            isLandscape = isLandscape,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(if (isLandscape) 8.dp else 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isLandscape) 2f else 1.5f)
        ) {
            if (isFullFlavor) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.onAction(CalculatorAction.ToggleMode) },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(if (state.isEngineeringMode) "Hide Engineering" else "Show Engineering")
                            }
                        }
                    }
                    
                    if (isLandscape) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Row(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                                EngineeringPad(
                                    onButtonClick = onAction,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isLandscape = true
                                )
                                OperationsPad(
                                    onButtonClick = onAction,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                            NumberPad(
                                onButtonClick = onAction,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        if (state.isEngineeringMode) {
                            EngineeringPad(
                                onButtonClick = onAction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.2f),
                                isLandscape = false
                            )
                            BasicPad(
                                onButtonClick = onAction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        } else {
                            BasicPad(
                                onButtonClick = onAction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }
            } else {
                BasicPad(
                    onButtonClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
    }
}
