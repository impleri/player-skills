package net.impleri.playerskills.server.bindings.block

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.events.BlockEvents
import net.impleri.slab.events.EventHandler
import net.impleri.slab.logging.Logger

case class OnBreak(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: BlockEvents = BlockEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends EventHandler with EventLogging {
  private[bindings] val handler: BlockEvents.OnBreak =
    (player, blockOpt, position, _, _) =>
      //    val replacedBlock = BlockRestrictions.getReplacement(player, originalBlockState, pos)
      //    val blockName = BlockRestrictions.getName(replacedBlock)
      //
      //    val tool = ItemRestrictions.getValue(player.mainHandItem)
      //    val toolName = ItemRestrictions.getName(tool)
      //
      //    if (!BlockRestrictions.isBreakable(player, replacedBlock, pos)) {
      //      PlayerSkillsLogger.BLOCKS.debug("${player.handle} cannot mine block $blockName")
      //      return EventResult.interruptFalse()
      //    }
      failOn {
        for {
          block <- blockOpt
          tool <- player.getItemInMainHand
          usable = itemRestrictionOps.isUsable(player, tool, position)
        } yield logEvent(player, s"mine block ${block.name} using ${tool.name}")(usable)
      }

  upstream.onBreak(handler)
}
