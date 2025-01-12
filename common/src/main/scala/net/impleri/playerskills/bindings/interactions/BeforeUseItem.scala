package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.events.ItemEventHandler
import net.impleri.slab.logging.Logger

case class BeforeUseItem(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: InteractionEvents = InteractionEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends ItemEventHandler with EventLogging {
  private[bindings] val handler: InteractionEvents.OnUseItem =
    (player: Player, hand: Hand) =>
      failOn {
        for {
          item <- player.getItemInHand(hand).filterNot(_.isDefault)
          usable = itemRestrictionOps.isUsable(player, item, None)
        } yield logEvent(player, s"use $item")(usable)
      }

  upstream.onRightClickItem(handler)
}
