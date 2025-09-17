package com.github.bartcowski.rps_battlegrounds.app

import com.github.bartcowski.rps_battlegrounds.config.GAME_SIZE_X
import com.github.bartcowski.rps_battlegrounds.config.GAME_SIZE_Y
import com.github.bartcowski.rps_battlegrounds.config.NUMBER_OF_SYMBOLS
import com.github.bartcowski.rps_battlegrounds.config.SYMBOL_SIZE
import com.github.bartcowski.rps_battlegrounds.model.*
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Component
class GameManager(
    private val gameStateBroadcaster: GameStateBroadcaster
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val gameStates: ConcurrentHashMap<String, GameState> = ConcurrentHashMap()

    init {
        runGameLoop()
    }

    fun getGameState(gameId: String): GameState {
        return gameStates[gameId]!! //TODO: throw and 404 probably
    }

    fun createNewGame(gameId: String): GameState {
        //TODO: maybe some checks to avoid two symbols being generated on top of each other?
        val rocks = generateSymbols(SymbolType.ROCK, NUMBER_OF_SYMBOLS, 0)
        val papers = generateSymbols(SymbolType.PAPER, NUMBER_OF_SYMBOLS, NUMBER_OF_SYMBOLS)
        val scissors = generateSymbols(SymbolType.SCISSORS, NUMBER_OF_SYMBOLS, 2 * NUMBER_OF_SYMBOLS)
        val allSymbols = (rocks + papers + scissors).toMutableList()
        val newGameState = GameState(gameId, allSymbols, GameStatus.CREATED)
        gameStates.putIfAbsent(gameId, newGameState)
        return newGameState
    }

    fun activateGame(gameId: String) {
        if (!gameStates.containsKey(gameId)) {
            throw IllegalArgumentException("game with ID '$gameId' not found")
        }
        gameStates.computeIfPresent(gameId) { _, gameState ->
            gameState.status = GameStatus.ACTIVE
            gameState
        }
    }

    fun applyUpgrade(upgradeType: UpgradeType, symbolId: Int, gameId: String, player: SymbolType) {
        if (!gameStates.containsKey(gameId)) {
            throw IllegalArgumentException("game with ID '$gameId' not found")
        }
        gameStates.computeIfPresent(gameId) { _, gameState ->
            gameState.applyUpgrade(symbolId, upgradeType)
            gameState
        }
    }

    fun applySpell(spellType: SpellType, position: Position, gameId: String, player: SymbolType) {
        if (!gameStates.containsKey(gameId)) {
            throw IllegalArgumentException("game with ID '$gameId' not found")
        }
        gameStates.computeIfPresent(gameId) { _, gameState ->
            gameState.applySpell(spellType, position, player)
            gameState
        }
    }

    fun getGameStateOrThrow(gameId: String): GameState {
        return gameStates[gameId]!!
    }

    private fun generateSymbols(type: SymbolType, amount: Int, startId: Int): MutableList<Symbol> {
        val symbols = mutableListOf<Symbol>()
        var id = startId
        repeat(amount) { _ ->
            val x = Random.nextInt(0, GAME_SIZE_X - SYMBOL_SIZE)
            val y = Random.nextInt(0, GAME_SIZE_Y - SYMBOL_SIZE)
            symbols.add(Symbol(id, type, Position(x.toDouble(), y.toDouble())))
            id++
        }
        return symbols
    }

    private fun runGameLoop() {
        //TODO: currently it's one coroutine = one game loop = 1 thread running all games
        // the problem is not that a single game is expensive to run as it's only around ~100-150 symbols (optimizations like spatial partitioning won't do much)
        // the problem is in dozens/hundreds of games being played at the same time, single thread might have problems handling all that
        // IDEA: have X (number of CPU?) gameState maps, run 1 loop per 1 map, load balance new games - add new game to map with fewest active games
        scope.launch {
            while (isActive) {
                gameStates.values.forEach { gameState ->
                    if (gameState.status == GameStatus.ACTIVE) {
                        gameState.updateGameState()
                        gameStateBroadcaster.broadcast(gameState.gameId, gameState)
                    }
                }
                delay(50) // 20 fps
            }
        }
    }
}
