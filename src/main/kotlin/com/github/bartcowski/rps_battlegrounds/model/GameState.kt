package com.github.bartcowski.rps_battlegrounds.model

import com.github.bartcowski.rps_battlegrounds.config.SYMBOL_SIZE
import com.github.bartcowski.rps_battlegrounds.config.SYMBOL_SPEED
import kotlin.math.pow
import kotlin.math.sqrt

class GameState(
    val gameId: String,
    val symbols: MutableList<Symbol>,
    var status: GameStatus) {

    fun updateGameState() {
        for (symbol in symbols) {
            val typeToHunt = getTypeToHunt(symbol.type)
            var closestSymbol: Symbol? = null
            var shortestDistance: Double = Double.MAX_VALUE
            for (huntedSymbol in symbols) {
                if (huntedSymbol.type != typeToHunt) {
                    continue
                }
                val distance = calculateDistance(symbol.position, huntedSymbol.position)
                if (distance >= shortestDistance) {
                    continue
                }
                closestSymbol = huntedSymbol
                shortestDistance = distance
            }
            if (closestSymbol == null) {
                continue
            }
            //TODO: I should keep the old position somewhere (inside Symbol class?)
            // so that next symbols move according to it and simulate parallel movement, now they will read updated position
            val dx = closestSymbol.position.x - symbol.position.x
            val dy = closestSymbol.position.y - symbol.position.y
            val directionX = dx / shortestDistance
            val directionY = dy / shortestDistance
            val velocityX = directionX * SYMBOL_SPEED
            val velocityY = directionY * SYMBOL_SPEED
            symbol.position.x += velocityX
            symbol.position.y += velocityY
        }
    }

    fun processCollisions() {
        val symbolsToDelete = mutableListOf<Symbol>()
        for (symbol in symbols) {
            val typeToHunt = getTypeToHunt(symbol.type)
            for (huntedSymbol in symbols) {
                if (huntedSymbol.type != typeToHunt) {
                    continue
                }
                if (symbol.position.x + SYMBOL_SIZE > huntedSymbol.position.x &&
                    symbol.position.x < huntedSymbol.position.x + SYMBOL_SIZE &&
                    symbol.position.y + SYMBOL_SIZE > huntedSymbol.position.y &&
                    symbol.position.y < huntedSymbol.position.y + SYMBOL_SIZE) {
                    symbolsToDelete.add(huntedSymbol)
                }
            }
        }
        symbols.removeAll(symbolsToDelete)
    }

    private fun calculateDistance(from: Position, to: Position): Double {
        return sqrt((to.x - from.x).pow(2) + (to.y - from.y).pow(2))
    }

    private fun getTypeToHunt(type: SymbolType): SymbolType {
        return when (type) {
            SymbolType.ROCK -> SymbolType.SCISSORS
            SymbolType.PAPER -> SymbolType.ROCK
            SymbolType.SCISSORS -> SymbolType.PAPER
        }
    }
}
