package com.github.bartcowski.rps_battlegrounds.model

sealed class GameStateEvent {
    data class RemoveSymbol(val symbol: Symbol) : GameStateEvent()
    data class SpawnSymbol(val symbol: Symbol) : GameStateEvent()
    data class RemoveUpgrade(val symbol: Symbol) : GameStateEvent()
}
