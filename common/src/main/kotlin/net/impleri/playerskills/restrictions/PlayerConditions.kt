package net.impleri.playerskills.restrictions

import java.util.function.Predicate
import net.minecraft.world.entity.player.Player as MCPlayer

interface PlayerConditions<Player> {
  var rawCondition: Predicate<Player>

  val actualCondition: (MCPlayer) -> Boolean

  fun condition(predicate: Predicate<Player>): PlayerConditions<Player> {
    rawCondition = Predicate { player: Player? -> player?.let { predicate.test(it) } ?: true }
    return this
  }

  fun unless(predicate: Predicate<Player>): PlayerConditions<Player> {
    rawCondition = Predicate { player: Player? -> player?.let { !predicate.test(it) } ?: true }
    return this
  }
}
