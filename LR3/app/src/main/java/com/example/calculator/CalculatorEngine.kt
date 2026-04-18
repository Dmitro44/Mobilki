package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.util.Stack

class CalculatorEngine {
    val mathContext: MathContext? = MathContext.UNLIMITED
    fun evaluate(expression: String): String {
        try {
            val tokens = tokenize(expression)
            val result = evaluateTokens(tokens)
            return formatResult(result)
        } catch (e: Exception) {
            return "Error"
        }
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            if (c.isWhitespace()) {
                i++
                continue
            }
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    sb.append(expression[i])
                    i++
                }
                tokens.add(sb.toString())
            } else {
                val str = c.toString()
                if (c == '-') {
                    val isUnary = tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "^", "√", "(", "u-")
                    if (isUnary) {
                        tokens.add("u-")
                    } else {
                        tokens.add(str)
                    }
                } else {
                    tokens.add(str)
                }
                i++
            }
        }
        return tokens
    }

    private fun evaluateTokens(tokens: List<String>): BigDecimal {
        val values = Stack<BigDecimal>()
        val ops = Stack<String>()

        val precedence = mapOf(
            "+" to 1, "-" to 1,
            "*" to 2, "/" to 2,
            "^" to 3,
            "u-" to 4, "√" to 4
        )
        
        val isRightAssociative = setOf("^", "u-", "√")

        for (token in tokens) {
            when {
                isNumber(token) -> {
                    values.push(BigDecimal(token))
                }
                token == "(" -> {
                    ops.push(token)
                }
                token == ")" -> {
                    while (ops.isNotEmpty() && ops.peek() != "(") {
                        applyTop(ops, values)
                    }
                    if (ops.isNotEmpty()) ops.pop() // pop '('
                }
                token == "!" -> {
                    if (values.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val a = values.pop()
                    values.push(factorial(a))
                }
                token == "%" -> {
                    if (values.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val a = values.pop()
                    if (ops.isNotEmpty() && (ops.peek() == "+" || ops.peek() == "-")) {
                        if (values.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                        val b = values.peek()
                        values.push(b.multiply(a).divide(BigDecimal(100), mathContext))
                    } else {
                        values.push(a.divide(BigDecimal(100), mathContext))
                    }
                }
                token in precedence -> {
                    val p = precedence[token]!!
                    while (ops.isNotEmpty() && ops.peek() != "(") {
                        val top = ops.peek()
                        val topP = precedence[top] ?: 0
                        
                        val condition = if (token in isRightAssociative) {
                            topP > p
                        } else {
                            topP >= p
                        }
                        
                        if (condition) {
                            applyTop(ops, values)
                        } else {
                            break
                        }
                    }
                    ops.push(token)
                }
                else -> throw IllegalArgumentException("Unknown token: $token")
            }
        }

        while (ops.isNotEmpty()) {
            applyTop(ops, values)
        }

        if (values.size != 1) throw IllegalArgumentException("Invalid syntax")
        return values.pop()
    }

    private fun applyTop(ops: Stack<String>, values: Stack<BigDecimal>) {
        if (ops.isEmpty()) throw IllegalArgumentException("Invalid syntax")
        val op = ops.pop()
        
        if (op == "u-") {
            if (values.isEmpty()) throw IllegalArgumentException("Invalid syntax")
            val a = values.pop()
            values.push(a.negate())
            return
        }

        if (op == "√") {
            if (values.isEmpty()) throw IllegalArgumentException("Invalid syntax")
            val a = values.pop()
            val result = BigDecimal(Math.sqrt(a.toDouble()), mathContext)
            values.push(result)
            return
        }

        if (values.size < 2) throw IllegalArgumentException("Invalid syntax")
        val b = values.pop()
        val a = values.pop()

        val res = when (op) {
            "+" -> a.add(b, mathContext)
            "-" -> a.subtract(b, mathContext)
            "*" -> a.multiply(b, mathContext)
            "/" -> {
                if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                try {
                    a.divide(b, mathContext)
                } catch (e: ArithmeticException) {
                    a.divide(b, MathContext.DECIMAL64)
                }
            }
            "^" -> {
                BigDecimal(Math.pow(a.toDouble(), b.toDouble()), mathContext)
            }
            else -> throw IllegalArgumentException("Unknown operator: $op")
        }
        values.push(res)
    }

    private fun factorial(n: BigDecimal): BigDecimal {
        val intVal = n.toInt()
        if (intVal < 0 || BigDecimal(intVal).compareTo(n) != 0) throw IllegalArgumentException("Factorial of non-integer")
        var res = BigDecimal.ONE
        for (i in 2..intVal) {
            res = res.multiply(BigDecimal(i))
        }
        return res
    }

    private fun isNumber(s: String): Boolean {
        return try {
            BigDecimal(s)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun formatResult(result: BigDecimal): String {
        if (result.compareTo(BigDecimal.ZERO) == 0) return "0"
        return result.stripTrailingZeros().toPlainString()
    }
}
