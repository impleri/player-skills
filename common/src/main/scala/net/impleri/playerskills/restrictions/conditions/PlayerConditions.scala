package net.impleri.playerskills.restrictions.conditions

import net.impleri.slab.entity.Player

trait PlayerConditions {
  var condition: Player => Boolean = _ => true

  def predicate(predicate: Player => Boolean): Unit =
    condition = predicate

  def unless(predicate: Player => Boolean): Unit =
    condition = (player: Player) => !predicate(player)
}
