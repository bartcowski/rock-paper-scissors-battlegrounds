package com.github.bartcowski.rps_battlegrounds.model

data class Symbol(
    val id: Int,
    val type: SymbolType,
    val position: Position,
    var upgrade: Upgrade? = null,
)
