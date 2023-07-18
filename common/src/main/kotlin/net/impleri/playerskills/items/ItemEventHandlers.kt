package net.impleri.playerskills.items

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.events.common.TickEvent
import dev.architectury.utils.value.IntValue
import net.impleri.playerskills.api.ItemRestrictions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class ItemEventHandlers {
  fun registerEventHandlers() {
    TickEvent.PLAYER_POST.register(TickEvent.Player { onPlayerTick(it) })

    PlayerEvent.PICKUP_ITEM_PRE.register(
      PlayerEvent.PickupItemPredicate { player: Player, _: Entity, stack: ItemStack ->
        beforePlayerPickup(
          player,
          stack,
        )
      },
    )

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

    EntityEvent.LIVING_HURT.register(
      EntityEvent.LivingHurt { entity: LivingEntity, source: DamageSource, amount: Float ->
        beforePlayerAttack(
          entity,
          source,
          amount,
        )
      },
    )

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

  private fun onPlayerTick(player: Player) {
    if (player.getLevel().isClientSide) {
      return
    }
    val inventory = player.inventory

    // Move unwearable items from armor and offhand into normal inventory
    ItemRestrictions.filterFromList(player, inventory.armor)
    ItemRestrictions.filterFromList(player, inventory.offhand)

    // Get unholdable items from inventory
    val itemsToRemove = ItemRestrictions.getItemsToRemove(player, inventory.items)

    // Drop the unholdable items from the normal inventory
    if (itemsToRemove.isNotEmpty()) {
      ItemSkills.LOGGER.debug("${player.name} is holding ${itemsToRemove.size} item(s) that should be dropped")
      itemsToRemove.forEach(ItemRestrictions.dropFromInventory(player))
    }
  }

  private fun beforePlayerPickup(player: Player, stack: ItemStack): EventResult {
    val item = ItemRestrictions.getValue(stack)
    if (ItemRestrictions.canHold(player, item, null)) {
      return EventResult.pass()
    }
    ItemSkills.LOGGER.debug("${player.name} is about to pickup ${ItemRestrictions.getName(item)}")
    return EventResult.interruptFalse()
  }

  private fun beforePlayerAttack(entity: LivingEntity, source: DamageSource, amount: Float): EventResult {
    val attacker = source.entity
    if (attacker is Player) {
      val weapon: Item = ItemRestrictions.getValue(attacker.mainHandItem)
      if (!ItemRestrictions.canAttackWith(attacker, weapon, null)) {
        ItemSkills.LOGGER.debug(
          "${attacker.name} was about to attack ${entity.name} using ${ItemRestrictions.getName(weapon)} for $amount damage",
        )
        return EventResult.interruptFalse()
      }
    }
    return EventResult.pass()
  }

  private fun beforeMine(
    pos: BlockPos,
    state: BlockState,
    player: ServerPlayer,
  ): EventResult {
    val tool = ItemRestrictions.getValue(player.mainHandItem)
    if (ItemRestrictions.canUse(player, tool, pos)) {
      return EventResult.pass()
    }
    ItemSkills.LOGGER.debug(
      "${player.name} was about to mine ${state.block.name} using ${ItemRestrictions.getName(tool)}",
    )
    return EventResult.interruptFalse()
  }

  private fun beforeInteractEntity(player: Player, entity: Entity, hand: InteractionHand): EventResult {
    val tool = ItemRestrictions.getItemUsed(player, hand)
    if (ItemRestrictions.canUse(player, tool, null)) {
      return EventResult.pass()
    }
    ItemSkills.LOGGER.debug(
      "${player.name} was about to interact with entity ${entity.name} using ${ItemRestrictions.getName(tool)}",
    )
    return EventResult.interruptFalse()
  }

  private fun beforeUseItem(player: Player, hand: InteractionHand): CompoundEventResult<ItemStack> {
    val tool = ItemRestrictions.getItemUsed(player, hand)
    if (ItemRestrictions.canUse(player, tool, null)) {
      return CompoundEventResult.pass()
    }
    ItemSkills.LOGGER.debug("${player.name} is about to use ${ItemRestrictions.getName(tool)}")
    return CompoundEventResult.interruptFalse(null)
  }

  private fun beforeUseItemBlock(player: Player, hand: InteractionHand, pos: BlockPos): EventResult {
    val tool = ItemRestrictions.getItemUsed(player, hand)
    if (ItemRestrictions.isDefaultItem(tool) || ItemRestrictions.canUse(player, tool, pos)) {
      return EventResult.pass()
    }

    val blockName = player.level.getBlockState(pos).block.name
    val itemName = ItemRestrictions.getName(tool)
    ItemSkills.LOGGER.debug("${player.name} is about to interact with block $blockName using $itemName")

    return EventResult.interruptFalse()
  }

  companion object {
    private val INSTANCE = ItemEventHandlers()

    fun init() {
      INSTANCE.registerEventHandlers()
    }
  }
}
