package net.impleri.playerskills.events.handlers

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.InteractionEvent
import net.impleri.playerskills.api.BlockRestrictions
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.api.MobRestrictions
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class InteractionEvents {
  fun registerEventHandlers() {
    InteractionEvent.LEFT_CLICK_BLOCK.register(
      InteractionEvent.LeftClickBlock { player: Player, hand: InteractionHand, pos: BlockPos, _: Direction ->
        beforeUseItemBlock(
          player,
          hand,
          pos,
        )
      },
    )

    InteractionEvent.RIGHT_CLICK_BLOCK.register(
      InteractionEvent.RightClickBlock { player: Player, hand: InteractionHand, pos: BlockPos, _: Direction ->
        beforeUseItemBlock(
          player,
          hand,
          pos,
        )
      },
    )

    InteractionEvent.RIGHT_CLICK_ITEM.register(
      InteractionEvent.RightClickItem { player: Player, hand: InteractionHand ->
        beforeUseItem(
          player,
          hand,
        )
      },
    )

    InteractionEvent.INTERACT_ENTITY.register(
      InteractionEvent.InteractEntity { player: Player, entity: Entity, hand: InteractionHand ->
        beforeInteractEntity(
          player,
          entity,
          hand,
        )
      },
    )
  }

  private fun beforeInteractEntity(player: Player, entity: Entity, hand: InteractionHand): EventResult {
    val mobType = MobRestrictions.getName(entity.type)
    if (!MobRestrictions.canInteractWith(entity.type, player)) {
      PlayerSkillsLogger.MOBS.debug("${player.name.string} cannot interact with entity $mobType")
      return EventResult.interruptFalse()
    }

    val item = ItemRestrictions.getItemUsed(player, hand)
    val itemName = ItemRestrictions.getName(item)

    if (!ItemRestrictions.canUse(player, item, null)) {
      PlayerSkillsLogger.ITEMS.debug("${player.name.string} cannot interact with entity $mobType using $itemName")
      return EventResult.interruptFalse()
    }

    PlayerSkillsLogger.SKIPS.debug("${player.name.string} is going to interact with entity $mobType using $itemName")
    return EventResult.pass()
  }

  private fun beforeUseItem(player: Player, hand: InteractionHand): CompoundEventResult<ItemStack> {
    val item = ItemRestrictions.getItemUsed(player, hand)
    val itemName = ItemRestrictions.getName(item)

    if (ItemRestrictions.canUse(player, item, null)) {
      PlayerSkillsLogger.ITEMS.debug("${player.name.string} cannot use $itemName")
      return CompoundEventResult.interruptFalse(null)
    }

    PlayerSkillsLogger.SKIPS.debug("${player.name.string} is going to use $itemName")
    return CompoundEventResult.pass()
  }

  private fun beforeUseItemBlock(player: Player, hand: InteractionHand, pos: BlockPos): EventResult {
    val blockState = BlockRestrictions.getBlockState(pos, player.getLevel())
    val replacement = BlockRestrictions.getReplacement(player, blockState, pos)
    val blockName = BlockRestrictions.getName(replacement)

    if (!BlockRestrictions.isUsable(player, replacement, pos)) {
      PlayerSkillsLogger.BLOCKS.debug("${player.name.string} cannot interact with block $blockName")
      return EventResult.interruptFalse()
    }

    val tool = ItemRestrictions.getItemUsed(player, hand)
    val toolName = ItemRestrictions.getName(tool)

    if (ItemRestrictions.isDefaultItem(tool) || ItemRestrictions.canUse(player, tool, pos)) {
      PlayerSkillsLogger.ITEMS.debug("${player.name.string} cannot interact with block $blockName using $toolName")
      return EventResult.pass()
    }

    PlayerSkillsLogger.SKIPS.debug("${player.name.string} is going to interact with block $blockName using $toolName")

    return EventResult.interruptFalse()
  }
}
