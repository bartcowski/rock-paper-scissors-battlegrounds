package com.github.bartcowski.rps_battlegrounds.model

interface Upgrade {
    fun onCollision(ctx: CollisionContext): List<GameStateEvent>
    fun getType(): UpgradeType

    companion object {
        fun fromUpgradeType(upgradeType: UpgradeType): Upgrade {
            return when (upgradeType) {
                UpgradeType.BOMBER -> BomberUpgrade()
                UpgradeType.COUNTER_ATTACK -> CounterAttackUpgrade()
                UpgradeType.NECROMANCER -> NecromancerUpgrade()
            }
        }
    }
}

class BomberUpgrade : Upgrade {
    companion object {
        private const val EXPLOSION_RADIUS = 50
    }

    override fun onCollision(ctx: CollisionContext): List<GameStateEvent> {
        val affectedSymbols = ctx.allSymbols.filter { calculateDistance(ctx.huntedSymbol.position, it.position) <= EXPLOSION_RADIUS }
        return affectedSymbols.map { GameStateEvent.RemoveSymbol(it) }
    }

    override fun getType(): UpgradeType {
        return UpgradeType.BOMBER
    }
}

class CounterAttackUpgrade : Upgrade {
    override fun onCollision(ctx: CollisionContext): List<GameStateEvent> {
        return listOf(GameStateEvent.RemoveSymbol(ctx.symbol), GameStateEvent.RemoveUpgrade(ctx.huntedSymbol))
    }

    override fun getType(): UpgradeType {
        return UpgradeType.COUNTER_ATTACK
    }
}

class NecromancerUpgrade : Upgrade {
    override fun onCollision(ctx: CollisionContext): List<GameStateEvent> {
        val transformedSymbol = Symbol(
            ctx.huntedSymbol.id,
            ctx.symbol.type,
            ctx.huntedSymbol.position,
        )
        return listOf(GameStateEvent.RemoveSymbol(ctx.huntedSymbol), GameStateEvent.SpawnSymbol(transformedSymbol))
    }

    override fun getType(): UpgradeType {
        return UpgradeType.NECROMANCER
    }
}
