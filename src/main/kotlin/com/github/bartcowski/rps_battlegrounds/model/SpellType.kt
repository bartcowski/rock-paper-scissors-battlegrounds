package com.github.bartcowski.rps_battlegrounds.model

enum class SpellType {
    FREEZE, DECOY
}

data class Spell(
    val spellType: SpellType,
    val position: Position,
    val playedBy: SymbolType,
)
