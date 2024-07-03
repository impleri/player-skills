package net.impleri.playerskills.integrations.trinkets.facade

import dev.emi.trinkets.api.TrinketsApi
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Player

import scala.jdk.OptionConverters._

case class Trinkets() {
  def getTrinketsFor(player: Player.Any): Option[Trinket] = {
    TrinketsApi.getTrinketComponent(player.value.asInstanceOf[Entity.Living])
      .toScala
      .map(Trinket)
  }
}
