package com.example.seabattle.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardGeneratorTest {

    @Test
    fun generateFleet_createsExpectedShipSizesWithoutOverlap() {
        val ships = BoardGenerator.generateFleet(Random(42))

        assertEquals(FleetRules.REQUIRED_SHIP_SIZES.sortedDescending(), ships.map { it.size }.sortedDescending())

        val allCells = ships.flatMap { it.cells }
        assertEquals(allCells.size, allCells.toSet().size)
        assertTrue(allCells.all { it in 0 until FleetRules.BOARD_CELL_COUNT })
        assertTrue(FleetRules.isValidFleet(ships))
    }
}
