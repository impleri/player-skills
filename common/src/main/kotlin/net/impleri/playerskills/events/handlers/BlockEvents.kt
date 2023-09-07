package net.impleri.playerskills.events.handlers

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.utils.value.IntValue
import net.impleri.playerskills.api.BlockRestrictions
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class BlockEvents {
  fun registerEventHandlers() {
    BlockEvent.BREAK.register(
      BlockEvent.Break { _: Level, pos: BlockPos, state: BlockState, player: ServerPlayer, _: IntValue? ->
        beforeMine(
          pos,
          state,
          player,
        )
      },
    )
  }

  private fun beforeMine(
    pos: BlockPos,
    originalBlockState: BlockState,
    player: ServerPlayer,
  ): EventResult {
    val replacedBlock = BlockRestrictions.getReplacement(player, originalBlockState, pos)
    val blockName = BlockRestrictions.getName(replacedBlock)

    val tool = ItemRestrictions.getValue(player.mainHandItem)
    val toolName = ItemRestrictions.getName(tool)

    if (!BlockRestrictions.isBreakable(player, replacedBlock, pos)) {
      PlayerSkillsLogger.BLOCKS.debug("${player.name.string} cannot mine block $blockName")
      return EventResult.interruptFalse()
    }

    if (!ItemRestrictions.canUse(player, tool, pos)) {
      PlayerSkillsLogger.ITEMS.debug("${player.name.string} cannot mine block $blockName using $toolName")
      return EventResult.interruptFalse()
    }

    PlayerSkillsLogger.SKIPS.debug("${player.name.string} is going to mine block $blockName using $toolName")

    return EventResult.pass()
  }
}
