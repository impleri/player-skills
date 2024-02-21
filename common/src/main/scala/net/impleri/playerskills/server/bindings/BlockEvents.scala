package net.impleri.playerskills.server.bindings

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.Event
import dev.architectury.utils.value.IntValue
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.world.Block
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

case class BlockEvents(
  itemRestrictionOps: ItemRestrictionOps,
  onBreak: Event[BlockEvent.Break] = BlockEvent.BREAK,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  skipLogger: PlayerSkillsLogger = PlayerSkillsLogger.SKIPS,
) {
  def registerEvents(): Unit = {
    onBreak.register { (_: Level, pos: BlockPos, state: BlockState, player: ServerPlayer, _: IntValue) =>
      beforeMine(Player(player), Block(state), Position(pos))
    }
  }

  private[bindings] def beforeMine(
    player: Player[_],
    block: Block,
    pos: Position,
  ): EventResult = {
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

    if (!itemRestrictionOps.isUsable(player, player.getItemInMainHand, Option(pos))) {
      logger.debug(s"${player.name} cannot mine block ${block.name} using ${player.getItemInMainHand.name}")
      EventResult.interruptFalse()
    } else {
      skipLogger.debug(s"${player.name} is going to mine block ${block.name} using ${player.getItemInMainHand.name}")
      EventResult.pass()
    }
  }
}
