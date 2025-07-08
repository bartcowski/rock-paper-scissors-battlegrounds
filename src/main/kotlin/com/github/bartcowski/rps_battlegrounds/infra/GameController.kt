package com.github.bartcowski.rps_battlegrounds.infra

import com.github.bartcowski.rps_battlegrounds.app.GameManager
import com.github.bartcowski.rps_battlegrounds.model.GameState
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class GameController(
    private val gameManager: GameManager
) {

    //TODO: it can be done in initialGameState endpoint as well
    @GetMapping("/new-game-id")
    fun getNewGameId(): GameId {
        return GameId(UUID.randomUUID().toString())
    }

    @GetMapping("/game-state/{gameId}")
    fun getInitialGameState(@PathVariable gameId: String): GameState {
        val newGameState = gameManager.createNewGame(gameId)
        return newGameState
    }

    @PostMapping("/game-state/{gameId}")
    fun activateGame(@PathVariable gameId: String): ResponseEntity<Void> {
        gameManager.activateGame(gameId)
        return ResponseEntity.status(200).build()
    }

    data class GameId(val id: String)
}
