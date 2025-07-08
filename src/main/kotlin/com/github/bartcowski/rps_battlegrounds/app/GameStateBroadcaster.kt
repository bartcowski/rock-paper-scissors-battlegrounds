package com.github.bartcowski.rps_battlegrounds.app

import com.github.bartcowski.rps_battlegrounds.model.GameState

interface GameStateBroadcaster {
    fun broadcast(gameId: String, gameState: GameState)
}
