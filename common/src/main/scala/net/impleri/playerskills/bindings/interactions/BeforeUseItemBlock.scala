package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
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
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) extends EventHandler {
  private[bindings] val handler: InteractionEvents.OnClickBlock = (player: Player[_], pos: Option[Position], hand: Hand, _: Direction) => {
    //    val blockState = BlockRestrictions.getBlockState(pos, player.getLevel())
    //    val replacement = BlockRestrictions.getReplacement(player, blockState, pos)
    //    val blockName = BlockRestrictions.getName(replacement)
    //
    //    if (!BlockRestrictions.isUsable(player, replacement, pos)) {
    //      PlayerSkillsLogger.BLOCKS.debug("${player.name.string} cannot interact with block $blockName")
    //      return EventResult.interruptFalse()
    //    }

    val result = for {
      item <- player.getItemInHand(hand).filterNot(_.isDefault)
      usable = itemRestrictionOps.isUsable(player, item, pos)
    } yield {
      if (!usable) {
        logger.debug(s"${player.name} cannot interact with block using ${item.name}")
      } else {
        skipLogger.debug(s"${player.name} is going to interact with block using ${item.name}")
      }

      usable
    }

    failOn(result)
  }

  upstream.onLeftClickBlock(handler)
  upstream.onRightClickBlock(handler)
}
