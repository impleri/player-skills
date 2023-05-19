package net.impleri.playerskills.restrictions

interface PlayerConditions<Player> {
  var rawCondition: (Player) -> Boolean

  val condition: (net.minecraft.world.entity.player.Player) -> Boolean

  fun condition(predicate: (Player) -> Boolean): PlayerConditions<Player> {
    rawCondition = { player: Player? -> player?.let { predicate(it) } ?: true }
    return this
  }

  fun unless(predicate: (Player) -> Boolean): PlayerConditions<Player> {
    rawCondition = { player: Player? -> player?.let { !predicate(it) } ?: true }
    return this
  }
}
