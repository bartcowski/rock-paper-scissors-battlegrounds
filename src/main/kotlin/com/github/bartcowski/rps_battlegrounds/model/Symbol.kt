package com.github.bartcowski.rps_battlegrounds.model

data class Symbol(
    val id: Int,
    val type: SymbolType,
    val position: Position,
    var upgrade: Upgrade? = null,
) {
    val previousPosition: Position = Position(position.x, position.y)

    fun copyPositionToPreviousPosition() {
        previousPosition.x = position.x
        previousPosition.y = position.y
    }
}
