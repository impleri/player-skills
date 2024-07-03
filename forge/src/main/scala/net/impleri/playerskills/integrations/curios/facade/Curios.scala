package net.impleri.playerskills.integrations.curios.facade

import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Player
import top.theillusivec4.curios.api.CuriosApi

import scala.jdk.OptionConverters._

case class Curios() {
  def getCuriosFor(player: Player.Any): Option[Curio] = {
    CuriosApi.getCuriosHelper.getEquippedCurios(player.underlying.asInstanceOf[Entity.Living])
      .resolve()
      .toScala
      .map(Curio)
  }
}
