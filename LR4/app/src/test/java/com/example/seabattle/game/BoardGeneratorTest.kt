package com.example.seabattle.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardGeneratorTest {

    @Test
    fun generateFleet_createsExpectedShipSizesWithoutOverlap() {
        val ships = BoardGenerator.generateFleet(Random(42))

        assertEquals(listOf(3, 2, 2), ships.map { it.size })

        val allCells = ships.flatMap { it.cells }
        assertEquals(allCells.size, allCells.toSet().size)
        assertTrue(allCells.all { it in 0..24 })
    }
}
