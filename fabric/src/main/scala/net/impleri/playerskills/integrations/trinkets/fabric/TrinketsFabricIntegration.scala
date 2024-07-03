package net.impleri.playerskills.integrations.trinkets.fabric

import net.impleri.playerskills.integrations.trinkets.facade.Trinket
import net.impleri.playerskills.integrations.trinkets.facade.Trinkets
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Player
import net.impleri.slab.events.PlayerTickType
import net.impleri.slab.events.TickEvents

case class TrinketsFabricIntegration(
  private val itemRestrictions: ItemRestrictionOps = ItemRestrictionOps(),
  private val trinkets: Trinkets = Trinkets(),
  private val tickEvents: TickEvents = TickEvents(),
) {
  private def handle: TickEvents.OnPlayerTick = (player: Player.Any) => {
    trinkets.getTrinketsFor(player)
      .foreach(handleTrinket(player))
  }

  private def handleTrinket(player: Player.Any)(trinket: Trinket): Unit = {
    trinket.inventoryMap
      .toSeq
      .filterNot(t => itemRestrictions.isWearable(player, t._2))
      .foreach(
        t => {
          player.putInInventory(t._2)
          trinket.replace(t._1)
        },
      )
  }

  tickEvents.onPlayerStart(handle, PlayerTickType.Server())
}
