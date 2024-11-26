package net.impleri.playerskills.bindings.player

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.events.EventHandler
import net.impleri.slab.events.PlayerEvents
import net.impleri.slab.logging.Logger

case class BeforePlayerPickup(
  itemRestrictionOps: ItemRestrictionOps = ItemRestrictionOps(),
  upstream: PlayerEvents = PlayerEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends EventHandler {
  private[bindings] val handler: PlayerEvents.CanPickup =
    (player, itemOpt, _) =>
      itemOpt.fold(skip) { item =>
        if (!itemRestrictionOps.isHoldable(player, item)) {
          logger.debug(s"${player.handle} cannot pickup ${item.name}")
          fail
        } else {
          logger.trace(s"${player.handle} is going to pickup ${item.name}")
          skip
        }
      }

  upstream.canPickup(handler)
}
