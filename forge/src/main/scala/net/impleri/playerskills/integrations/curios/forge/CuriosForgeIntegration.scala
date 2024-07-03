package net.impleri.playerskills.integrations.curios.forge

import net.impleri.playerskills.integrations.curios.facade.Curio
import net.impleri.playerskills.integrations.curios.facade.Curios
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Player
import net.impleri.slab.events.PlayerTickType
import net.impleri.slab.events.TickEvents

case class CuriosForgeIntegration(
  private val itemRestrictions: ItemRestrictionOps = ItemRestrictionOps(),
  private val curios: Curios = Curios(),
  private val tickEvents: TickEvents = TickEvents(),
) {
  private def handle: TickEvents.OnPlayerTick = (player: Player.Any) => {
    curios.getCuriosFor(player).foreach(handleCurios(player))
  }

  private def handleCurios(player: Player.Any)(curio: Curio): Unit = {
    curio.inventoryMap
      .toSeq
      .filterNot(t => itemRestrictions.isWearable(player, t._2))
      .flatMap(t => curio.removeItem(t._1, t._2.getAmountInStack))
      .foreach(player.putInInventory)
  }

  tickEvents.onPlayerStart(handle, PlayerTickType.Server())
}
