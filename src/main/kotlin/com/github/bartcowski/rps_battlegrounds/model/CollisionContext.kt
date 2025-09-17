package com.github.bartcowski.rps_battlegrounds.model

data class CollisionContext(
    val symbol: Symbol,
    val huntedSymbol: Symbol,
    val allSymbols: List<Symbol>,
)
