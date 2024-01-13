package net.impleri.playerskills.restrictions.conditions

import net.impleri.playerskills.facades.minecraft.Player

trait PlayerConditions {
  var condition: Player[_] => Boolean = _ => true

  def predicate(predicate: Player[_] => Boolean): Unit = {
    condition = predicate
  }

  def unless(predicate: Player[_] => Boolean): Unit = {
    condition = (player: Player[_]) => !predicate(player)
  }
}
