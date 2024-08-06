package net.impleri.playerskills.server.bindings.player

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.events.TickEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger

import scala.util.chaining.scalaUtilChainingOps

case class OnPlayerTick(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: TickEvents = TickEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) {
  private def filterItemsNot(
    items: Map[Int, Item],
    f: Item => Boolean,
  ): Map[Int, Item] = {
    items.filterNot(_._2.isEmpty).filterNot(t => f(t._2))
  }

  private def filterWearable(
    player: Player[_],
    items: Map[Int, Item],
  ): Map[Int, Item] = {
    filterItemsNot(items, itemRestrictionOps.isWearable(player, _))
  }

  private def filterHoldable(
    player: Player[_],
    items: Map[Int, Item],
  ): Map[Int, Item] = {
    filterItemsNot(items, itemRestrictionOps.isHoldable(player, _))
  }

  private def moveToInventory(player: Player[_], f: Int => Unit)(
    tuple: (Int, Item),
  ): Unit = {
    val (index, item) = tuple

    player.putInInventory(item)
    f(index)
  }

  private[bindings] val handler: TickEvents.OnPlayerTick = player => {
    if (!player.isClientSide) {
      // Move unwearable items from armor into normal inventory
      filterWearable(player, player.armor).foreach(
        moveToInventory(player, player.emptyArmor),
      )

      filterHoldable(player, player.offHand).foreach(
        moveToInventory(player, player.emptyOffHand),
      )

      // Drop the unholdable items from the normal inventory
      filterHoldable(player, player.inventory).values
        .tap(r =>
          if (r.nonEmpty)
            logger.debug(
              s"${player.handle} is holding ${r.size} item(s) that should be dropped",
            ),
        )
        .foreach(player.toss)
    }
  }

  upstream.onPlayerEnd(handler)
}
