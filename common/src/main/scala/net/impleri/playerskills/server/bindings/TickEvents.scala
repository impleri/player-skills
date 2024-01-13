package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.TickEvent
import dev.architectury.event.Event
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class TickEvents(
  itemRestrictionOps: ItemRestrictionOps,
  onPlayerPost: Event[TickEvent.Player] = TickEvent.PLAYER_POST,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) {
  private[server] def registerEventHandlers(): Unit = {
    onPlayerPost.register { player => onPlayerTick(Player(player)) }
  }

  private def filterItemsNot(items: Map[Int, Item], f: Item => Boolean): Map[Int, Item] = {
    items.filterNot(_._2.isEmpty).filterNot(t => f(t._2))
  }

  private def filterWearable(player: Player[_], items: Map[Int, Item]): Map[Int, Item] = {
    filterItemsNot(items, itemRestrictionOps.isWearable(player, _))
  }

  private def filterHoldable(player: Player[_], items: Map[Int, Item]): Map[Int, Item] = {
    filterItemsNot(items, itemRestrictionOps.isHoldable(player, _))
  }

  private def moveToInventory(player: Player[_], f: Int => Unit)(tuple: (Int, Item)): Unit = {
    val (index, item) = tuple

    player.putInInventory(item)
    f(index)
  }

  private def onPlayerTick(player: Player[_]): Unit = {
    if (!player.isClientSide) {
      // Move unwearable items from armor and offhand into normal inventory
      filterWearable(player, player.armor).foreach(moveToInventory(player, player.emptyArmor))
      filterWearable(player, player.offHand).foreach(moveToInventory(player, player.emptyOffHand))

      val toRemove = filterHoldable(player, player.inventory).values

      // Drop the unholdable items from the normal inventory
      if (toRemove.nonEmpty) {
        logger.debug(s"${player.name} is holding ${toRemove.size} item(s) that should be dropped")
        toRemove.foreach(player.toss)
      }
    }
  }
}
