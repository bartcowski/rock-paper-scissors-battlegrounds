package com.github.bartcowski.rps_battlegrounds.infra.dto

import com.github.bartcowski.rps_battlegrounds.model.*

data class SymbolDTO(
    val id: Int,
    val type: String,
    val position: Position,
    var upgrade: String? = null,
) {
    companion object {
        fun fromDomain(symbol: Symbol): SymbolDTO {
            return SymbolDTO(
                symbol.id,
                symbol.type.name,
                symbol.position,
                symbol.upgrade?.getType()?.name,
            )
        }
    }
}
