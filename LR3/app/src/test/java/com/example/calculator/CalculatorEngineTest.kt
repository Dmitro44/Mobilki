package com.example.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    @Test
    fun testAddition() {
        assertEquals("5", engine.evaluate("2 + 3"))
        assertEquals("100", engine.evaluate("50 + 50"))
    }

    @Test
    fun testSubtraction() {
        assertEquals("-1", engine.evaluate("2 - 3"))
        assertEquals("0", engine.evaluate("50 - 50"))
    }

    @Test
    fun testMultiplication() {
        assertEquals("6", engine.evaluate("2 * 3"))
        assertEquals("-15", engine.evaluate("-3 * 5"))
    }

    @Test
    fun testDivision() {
        assertEquals("0.5", engine.evaluate("1 / 2"))
        assertEquals("-2.5", engine.evaluate("-5 / 2"))
    }

    @Test
    fun testDivisionByZero() {
        assertEquals("Error", engine.evaluate("1 / 0"))
    }

    @Test
    fun testPrecision() {
        assertEquals("0.3333333333333333", engine.evaluate("1 / 3"))
    }

    @Test
    fun testPercentages() {
        assertEquals("0.5", engine.evaluate("50 %"))
        assertEquals("55", engine.evaluate("50 + 10 %"))
    }

    @Test
    fun testEngineeringOperations() {
        assertEquals("3", engine.evaluate("√9"))
        assertEquals("8", engine.evaluate("2 ^ 3"))
        assertEquals("120", engine.evaluate("5 !"))
        assertEquals("1", engine.evaluate("0 !"))
    }

    @Test
    fun testParentheses() {
        assertEquals("14", engine.evaluate("2 * (3 + 4)"))
        assertEquals("10", engine.evaluate("(2 + 3) * 2"))
        assertEquals("20", engine.evaluate("100 / (2 + 3)"))
    }

    @Test
    fun testUnaryMinus() {
        assertEquals("-5", engine.evaluate("-5"))
        assertEquals("2", engine.evaluate("-(-2)"))
        assertEquals("-1", engine.evaluate("-(3 - 2)"))
    }

    @Test
    fun testDecimalFormatting() {
        assertEquals("5", engine.evaluate("5.00"))
        assertEquals("2.5", engine.evaluate("1.25 * 2.0"))
        assertEquals("0.1", engine.evaluate("0.1000"))
    }

    @Test
    fun testComplexExpressions() {
        assertEquals("10", engine.evaluate("2 * (3 + 2) ! / 24"))
        assertEquals("5", engine.evaluate("√(9 + 16)"))
        assertEquals("8", engine.evaluate("2 ^ (1 + 2)"))
    }
}
