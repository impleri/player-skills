package net.impleri.playerskills.bindings

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.Event
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class PlayerEvents(
  itemRestrictionOps: ItemRestrictionOps = ItemRestrictionOps(),
  onBeforePickupItem: Event[PlayerEvent.PickupItemPredicate] = PlayerEvent.PICKUP_ITEM_PRE,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  skipLogger: PlayerSkillsLogger = PlayerSkillsLogger.SKIPS,
) {
  private[playerskills] def registerEvents(): Unit = {
    onBeforePickupItem.register { (p, _, i) => beforePlayerPickup(Player(p), Item(i))
    }
  }

  private def beforePlayerPickup(player: Player[_], item: Item): EventResult = {
    if (!itemRestrictionOps.isHoldable(player, item)) {
      logger.debug(s"${player.name} cannot pickup ${item.name}")
      EventResult.interruptFalse()
    } else {
      skipLogger.debug(s"${player.name} is going to pickup ${item.name}")
      EventResult.pass()
    }
  }
}
