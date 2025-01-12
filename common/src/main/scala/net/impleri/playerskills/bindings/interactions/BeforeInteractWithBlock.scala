package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.{EventHandler, InteractionEvents}
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Direction.Direction
import net.impleri.slab.world.Position

case class BeforeInteractWithBlock(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: InteractionEvents = InteractionEvents(),
  logger: Logger = PlayerSkillsLogger.BLOCKS,
) extends EventHandler with EventLogging {
  private[bindings] val handler: InteractionEvents.OnClickBlock =
    (player: Player, pos: Option[Position], hand: Hand, _: Direction) =>
      //    val replacement = BlockRestrictions.getReplacement(player, blockState, pos)

      failOn {
        for {
          level <- player.level
          blockPos <- pos
          block <- level.getBlockAt(blockPos)
          item = player.getItemInHand(hand).filterNot(_.isDefault)
          // replacement should be handled within blockRestrictionOps opaquely
          usable = false // blockRestrictionOps.isUsable(player, blockPos, item)
        } yield logEvent(player, s"interact with block $block using $item")(usable)
      }

  upstream.onLeftClickBlock(handler)
  upstream.onRightClickBlock(handler)
}
