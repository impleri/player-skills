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
) extends ItemEventHandler {
  private[bindings] val handler: InteractionEvents.OnUseItem =
    (player: Player, hand: Hand) =>
      failOn {
        for {
          item <- player.getItemInHand(hand).filterNot(_.isDefault)
          usable = itemRestrictionOps.isUsable(player, item, None)
        } yield {
          if (!usable) {
            logger.debug(s"${player.handle} cannot use ${item.name}")
          } else {
            logger.trace(s"${player.handle} is going to use ${item.name}")
          }
          usable
        }
      }

  upstream.onRightClickItem(handler)
}
