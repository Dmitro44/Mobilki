package com.example.seabattle.game

import com.example.seabattle.model.Ship
import com.example.seabattle.model.ShipOrientation

object FleetRules {

    const val BOARD_SIZE = 10
    const val BOARD_CELL_COUNT = BOARD_SIZE * BOARD_SIZE
    val REQUIRED_SHIP_SIZES = listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)

    fun rowOf(cellIndex: Int): Int = cellIndex / BOARD_SIZE

    fun columnOf(cellIndex: Int): Int = cellIndex % BOARD_SIZE

    fun cellIndex(row: Int, column: Int): Int = row * BOARD_SIZE + column

    fun buildShip(startCell: Int, size: Int, orientation: ShipOrientation): Ship? {
        if (size !in REQUIRED_SHIP_SIZES || startCell !in 0 until BOARD_CELL_COUNT) return null

        val row = rowOf(startCell)
        val column = columnOf(startCell)
        val cells = buildList {
            repeat(size) { step ->
                val targetRow = if (orientation == ShipOrientation.HORIZONTAL) row else row + step
                val targetColumn = if (orientation == ShipOrientation.HORIZONTAL) column + step else column
                if (targetRow !in 0 until BOARD_SIZE || targetColumn !in 0 until BOARD_SIZE) return null
                add(cellIndex(targetRow, targetColumn))
            }
        }

        return Ship(size = size, cells = cells)
    }

    fun isValidShip(ship: Ship): Boolean {
        if (ship.size !in REQUIRED_SHIP_SIZES || ship.cells.size != ship.size) return false
        if (ship.cells.any { it !in 0 until BOARD_CELL_COUNT }) return false
        if (ship.cells.toSet().size != ship.cells.size) return false

        val rows = ship.cells.map(::rowOf).distinct()
        val columns = ship.cells.map(::columnOf).distinct()
        if (rows.size != 1 && columns.size != 1) return false

        val sortedCells = if (rows.size == 1) {
            ship.cells.sortedBy(::columnOf)
        } else {
            ship.cells.sortedBy(::rowOf)
        }

        return sortedCells.zipWithNext().all { (left, right) ->
            if (rows.size == 1) {
                columnOf(right) - columnOf(left) == 1
            } else {
                rowOf(right) - rowOf(left) == 1
            }
        }
    }

    fun overlaps(existingShips: List<Ship>, ship: Ship): Boolean {
        return existingShips.any { existing -> existing.cells.any(ship.cells::contains) }
    }

    fun touches(existingShips: List<Ship>, ship: Ship): Boolean {
        return existingShips.any { existing ->
            existing.cells.any { existingCell ->
                ship.cells.any { newCell ->
                    kotlin.math.abs(rowOf(existingCell) - rowOf(newCell)) <= 1 &&
                        kotlin.math.abs(columnOf(existingCell) - columnOf(newCell)) <= 1
                }
            }
        }
    }

    fun canPlaceShip(existingShips: List<Ship>, ship: Ship): Boolean {
        return isValidShip(ship) && !overlaps(existingShips, ship) && !touches(existingShips, ship)
    }

    fun isValidFleet(ships: List<Ship>): Boolean {
        if (ships.size != REQUIRED_SHIP_SIZES.size) return false
        if (!ships.all(::isValidShip)) return false
        if (ships.map { it.size }.sortedDescending() != REQUIRED_SHIP_SIZES.sortedDescending()) return false

        return ships.indices.all { index ->
            val ship = ships[index]
            val others = ships.filterIndexed { otherIndex, _ -> otherIndex != index }
            !overlaps(others, ship) && !touches(others, ship)
        }
    }

    fun remainingShipSizes(ships: List<Ship>): List<Int> {
        val remaining = REQUIRED_SHIP_SIZES.toMutableList()
        ships.map { it.size }.forEach { remaining.remove(it) }
        return remaining.sortedDescending()
    }

    fun findShipAt(ships: List<Ship>, cellIndex: Int): Ship? {
        return ships.firstOrNull { cellIndex in it.cells }
    }
}
