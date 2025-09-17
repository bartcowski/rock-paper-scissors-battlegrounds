package com.github.bartcowski.rps_battlegrounds.infra.dto

import com.github.bartcowski.rps_battlegrounds.model.*

data class GameStateDTO(
    val gameId: String,
    val symbols: List<SymbolDTO>,
    var status: String,
    val spells: List<SpellDTO>,
    var winner: String? = null
) {
    companion object {
        fun fromDomain(gameState: GameState): GameStateDTO {
            return GameStateDTO(
                gameState.gameId,
                gameState.symbols.map { SymbolDTO.fromDomain(it) },
                gameState.status.name,
                gameState.spells.map { SpellDTO.fromDomain(it) },
                gameState.winner?.name,
            )
        }
    }
}
