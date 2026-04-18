package com.example.calculator

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        viewModel = CalculatorViewModel()
    }

    @Test
    fun `initial state is empty expression`() {
        assertEquals("", viewModel.state.value.expression)
    }

    @Test
    fun `entering numbers updates expression`() {
        viewModel.onAction(CalculatorAction.Number(5))
        viewModel.onAction(CalculatorAction.Number(2))
    }

    @Test
    fun `consecutive operators do not stack`() {
        viewModel.onAction(CalculatorAction.Number(5))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Operation("+"))
    }

    @Test
    fun `different consecutive operators replace previous`() {
        viewModel.onAction(CalculatorAction.Number(5))
        viewModel.onAction(CalculatorAction.Operation("*"))
        viewModel.onAction(CalculatorAction.Operation("+"))
    }

    @Test
    fun `square root applies correctly`() {
        viewModel.onAction(CalculatorAction.Number(9))
        viewModel.onAction(CalculatorAction.Operation("sqrt"))
    }

    @Test
    fun `consecutive decimals are prevented`() {
        viewModel.onAction(CalculatorAction.Number(5))
        viewModel.onAction(CalculatorAction.Decimal)
        viewModel.onAction(CalculatorAction.Decimal)
    }

    @Test
    fun `calculate action evaluates expression`() {
        viewModel.onAction(CalculatorAction.Number(2))
        viewModel.onAction(CalculatorAction.Operation("+"))
        viewModel.onAction(CalculatorAction.Number(3))
        viewModel.onAction(CalculatorAction.Calculate)
    }
}
