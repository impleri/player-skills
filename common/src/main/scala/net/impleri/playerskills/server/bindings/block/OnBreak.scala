package net.impleri.playerskills.server.bindings.block

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.events.BlockEvents
import net.impleri.slab.events.EventHandler
import net.impleri.slab.logging.Logger

case class OnBreak(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: BlockEvents,
  logger: Logger = PlayerSkillsLogger.ITEMS,
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) extends EventHandler {
  private[bindings] val handler: BlockEvents.OnBreak = (player, blockOpt, position, _, _) => {
    //    val replacedBlock = BlockRestrictions.getReplacement(player, originalBlockState, pos)
    //    val blockName = BlockRestrictions.getName(replacedBlock)
    //
    //    val tool = ItemRestrictions.getValue(player.mainHandItem)
    //    val toolName = ItemRestrictions.getName(tool)
    //
    //    if (!BlockRestrictions.isBreakable(player, replacedBlock, pos)) {
    //      PlayerSkillsLogger.BLOCKS.debug("${player.name.string} cannot mine block $blockName")
    //      return EventResult.interruptFalse()
    //    }
    val result = for {
      block <- blockOpt
      tool <- player.getItemInMainHand
      usable = itemRestrictionOps.isUsable(player, tool, position)
    } yield {
      if (!usable) {
        logger.debug(s"${player.name} cannot mine block ${block.name} using ${tool.name}")
      } else {
        skipLogger.debug(s"${player.name} is going to mine block ${block.name} using ${tool.name}")
      }

      usable
    }

    failOn(result)
  }

  upstream.onBreak(handler)
}
