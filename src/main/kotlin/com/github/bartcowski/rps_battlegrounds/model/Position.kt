package com.github.bartcowski.rps_battlegrounds.model

import kotlin.math.pow
import kotlin.math.sqrt

data class Position(
    var x: Double,
    var y: Double
)

fun calculateDistance(from: Position, to: Position): Double {
    return sqrt((to.x - from.x).pow(2) + (to.y - from.y).pow(2))
}
