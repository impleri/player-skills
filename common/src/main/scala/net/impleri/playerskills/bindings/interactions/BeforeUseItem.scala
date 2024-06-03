package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.events.ItemEventHandler
import net.impleri.slab.logging.Logger

case class BeforeUseItem(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: InteractionEvents = InteractionEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) extends ItemEventHandler {
  private[bindings] val handler: InteractionEvents.OnUseItem = (player: Player[_], hand: Hand) => {
    val result = for {
      item <- player.getItemInHand(hand).filterNot(_.isDefault)
      usable = itemRestrictionOps.isUsable(player, item, None)
    } yield {
      if (!usable) {
        logger.debug(s"${player.name} cannot use ${item.name}")
      } else {
        skipLogger.debug(s"${player.name} is going to use ${item.name}")
      }
      usable
    }

    failOn(result)
  }

  upstream.onRightClickItem(handler)
}
