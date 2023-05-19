package net.impleri.playerskills.restrictions

interface PlayerConditions<Player> {
  var rawCondition: (Player) -> Boolean

  fun condition(predicate: (Player) -> Boolean): PlayerConditions<Player> {
    rawCondition = { player: Player? -> player?.let { predicate(it) } ?: true }
    return this
  }

  fun unless(predicate: (Player) -> Boolean): PlayerConditions<Player> {
    rawCondition = { player: Player? -> player?.let { !predicate(it) } ?: true }
    return this
  }
}
