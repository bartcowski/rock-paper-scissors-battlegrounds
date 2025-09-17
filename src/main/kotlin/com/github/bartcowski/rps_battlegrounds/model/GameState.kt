package com.github.bartcowski.rps_battlegrounds.model

import com.github.bartcowski.rps_battlegrounds.config.SYMBOL_SIZE
import com.github.bartcowski.rps_battlegrounds.config.SYMBOL_SPEED
import org.yaml.snakeyaml.util.Tuple
import kotlin.math.pow
import kotlin.math.sqrt

class GameState(
    val gameId: String,
    val symbols: MutableList<Symbol>,
    var status: GameStatus,
    val spells: MutableList<Spell> = mutableListOf(),
    var winner: SymbolType? = null
) {

    fun updateGameState() {
        if (status != GameStatus.ACTIVE) {
            return
        }
        val onlyOneSymbolTypeStillInGame = onlyOneSymbolTypeStillInGame()
        if (onlyOneSymbolTypeStillInGame._1()) {
            status = GameStatus.ENDED
            winner = onlyOneSymbolTypeStillInGame._2()
            return
        }

        processCollisions()

        // update previous position here instead of inside the loop to have it equal for all symbols in all iterations
        symbols.forEach { it.copyPositionToPreviousPosition() }

        for (symbol in symbols) {
            val typeToHunt = getTypeToHunt(symbol.type)
            var closestSymbol: Symbol? = null
            var shortestDistance: Double = Double.MAX_VALUE
            for (huntedSymbol in symbols) {
                if (huntedSymbol.type != typeToHunt) {
                    continue
                }
                val distance = calculateDistance(symbol.previousPosition, huntedSymbol.previousPosition)
                if (distance >= shortestDistance) {
                    continue
                }
                closestSymbol = huntedSymbol
                shortestDistance = distance
            }
            if (closestSymbol == null) {
                continue
            }
            val dx = closestSymbol.previousPosition.x - symbol.previousPosition.x
            val dy = closestSymbol.previousPosition.y - symbol.previousPosition.y
            val directionX = dx / shortestDistance
            val directionY = dy / shortestDistance
            val velocityX = directionX * SYMBOL_SPEED
            val velocityY = directionY * SYMBOL_SPEED
            symbol.position.x += velocityX
            symbol.position.y += velocityY
        }
    }

    private fun onlyOneSymbolTypeStillInGame(): Tuple<Boolean, SymbolType> {
        val distinctTypeSymbols = symbols.distinctBy { it.type }
        return if (distinctTypeSymbols.size == 1) {
            Tuple(true, distinctTypeSymbols[0].type)
        } else {
            Tuple(false, null)
        }
    }

    private fun processCollisions() {
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

    fun applyUpgrade(symbolId: Int, upgrade: Upgrade) {
        //NoSuchElementException thrown
        symbols.first { it.id == symbolId }.upgrade = upgrade
    }

    fun applySpell(spellType: SpellType, position: Position, player: SymbolType) {
        spells.add(Spell(spellType, position, player))
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
