package com.example.seabattle.game

import com.example.seabattle.model.Ship
import kotlin.random.Random

object BoardGenerator {

    private const val BoardSize = 5
    private val ShipSizes = listOf(3, 2, 2)

    fun generateFleet(random: Random = Random.Default): List<Ship> {
        val occupied = mutableSetOf<Int>()
        val ships = mutableListOf<Ship>()

        ShipSizes.forEach { size ->
            var ship: Ship? = null
            while (ship == null) {
                val horizontal = random.nextBoolean()
                val row = random.nextInt(BoardSize)
                val col = random.nextInt(BoardSize)
                val cells = mutableListOf<Int>()

                repeat(size) { step ->
                    val nextRow = if (horizontal) row else row + step
                    val nextCol = if (horizontal) col + step else col
                    if (nextRow !in 0 until BoardSize || nextCol !in 0 until BoardSize) {
                        cells.clear()
                        return@repeat
                    }
                    cells += nextRow * BoardSize + nextCol
                }

                if (cells.size == size && cells.none(occupied::contains)) {
                    occupied += cells
                    ship = Ship(size = size, cells = cells)
                }
            }

            ships += requireNotNull(ship)
        }

        return ships
    }
}
