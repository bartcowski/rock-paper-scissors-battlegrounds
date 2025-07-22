package com.github.bartcowski.rps_battlegrounds.infra

import com.github.bartcowski.rps_battlegrounds.app.GameManager
import com.github.bartcowski.rps_battlegrounds.model.GameState
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/debug")
class DebugController(
    private val gameManager: GameManager
) {
    @GetMapping("/game-state/{gameId}")
    fun getGameState(@PathVariable gameId: String): GameState {
        return gameManager.getGameStateOrThrow(gameId)
    }
}
