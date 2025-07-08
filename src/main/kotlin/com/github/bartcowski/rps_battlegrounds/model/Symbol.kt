package com.github.bartcowski.rps_battlegrounds.model

data class Symbol(
    val type: SymbolType,
    val position: Position
)

data class Position(
    var x: Double,
    var y: Double
)
