package com.github.bartcowski.rps_battlegrounds.infra

import com.github.bartcowski.rps_battlegrounds.app.GameManager
import com.github.bartcowski.rps_battlegrounds.model.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @GetMapping("/upgrades-and-spells")
    fun getUpgradesAndSpells(): UpgradesAndSpells {
        val upgrades = Upgrade.entries.map { e -> e.name }.toList()
        val spells = SpellType.entries.map { e -> e.name }.toList()
        return UpgradesAndSpells(upgrades, spells)
    }

    @PostMapping("/game-state/{gameId}/upgrade")
    fun playUpgrade(@PathVariable gameId: String, @RequestBody playedUpgrade: PlayedUpgrade): ResponseEntity<Void> {
        val upgrade = Upgrade.valueOf(playedUpgrade.upgrade)
        gameManager.applyUpgrade(upgrade, playedUpgrade.symbolId, gameId, playedUpgrade.player)
        return ResponseEntity.status(200).build()
    }

    @PostMapping("/game-state/{gameId}/spell")
    fun playSpell(@PathVariable gameId: String, @RequestBody playedSpell: PlayedSpell): ResponseEntity<Void> {
        val spellType = SpellType.valueOf(playedSpell.spell)
        val position = Position(playedSpell.posX, playedSpell.posY)
        val player = SymbolType.valueOf(playedSpell.player)
        gameManager.applySpell(spellType, position, gameId, player)
        return ResponseEntity.status(200).build()
    }

    data class GameId(val id: String)
    data class UpgradesAndSpells(val upgrades: List<String>, val spells: List<String>)
    data class PlayedUpgrade(val upgrade: String, val symbolId: Int, val player: String)
    data class PlayedSpell(val spell: String, val posX: Double, val posY: Double, val player: String)
}
