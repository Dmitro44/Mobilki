package com.example.seabattle.game

import com.example.seabattle.model.Ship
import com.example.seabattle.model.ShipOrientation
import kotlin.random.Random

object BoardGenerator {

    fun generateFleet(random: Random = Random.Default): List<Ship> {
        val ships = mutableListOf<Ship>()

        FleetRules.REQUIRED_SHIP_SIZES.forEach { size ->
            var ship: Ship? = null
            while (ship == null) {
                val orientation = if (random.nextBoolean()) {
                    ShipOrientation.HORIZONTAL
                } else {
                    ShipOrientation.VERTICAL
                }
                val row = random.nextInt(FleetRules.BOARD_SIZE)
                val col = random.nextInt(FleetRules.BOARD_SIZE)
                val candidate = FleetRules.buildShip(
                    startCell = FleetRules.cellIndex(row, col),
                    size = size,
                    orientation = orientation,
                )

                if (candidate != null && FleetRules.canPlaceShip(ships, candidate)) {
                    ship = candidate
                }
            }

            ships += requireNotNull(ship)
        }

        return ships
    }
}
