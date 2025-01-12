package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.EventHandler
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Direction.Direction
import net.impleri.slab.world.Position

case class BeforeUseItemBlock(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: InteractionEvents = InteractionEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends EventHandler with EventLogging {
  private[bindings] val handler: InteractionEvents.OnClickBlock =
    (player: Player, pos: Option[Position], hand: Hand, _: Direction) =>
      failOn {
        for {
          item <- player.getItemInHand(hand).filterNot(_.isDefault)
          usable = itemRestrictionOps.isUsable(player, item, pos)
        } yield logEvent(player, s"interact with block using $item")(usable)
      }

  upstream.onLeftClickBlock(handler)
  upstream.onRightClickBlock(handler)
}
