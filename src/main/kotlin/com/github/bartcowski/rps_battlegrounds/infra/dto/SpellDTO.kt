package com.github.bartcowski.rps_battlegrounds.infra.dto

import com.github.bartcowski.rps_battlegrounds.model.Position
import com.github.bartcowski.rps_battlegrounds.model.Spell

data class SpellDTO(
    val spellType: String,
    val position: Position,
    val playedBy: String,
) {
    companion object {
        fun fromDomain(spell: Spell): SpellDTO {
            return SpellDTO(
                spell.spellType.name,
                spell.position,
                spell.playedBy.name,
            )
        }
    }
}
