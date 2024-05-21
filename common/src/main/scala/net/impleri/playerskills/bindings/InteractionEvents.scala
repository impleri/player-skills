package net.impleri.playerskills.bindings

import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.CompoundEventResult
import dev.architectury.event.Event
import dev.architectury.event.EventResult
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.EventUtils
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.item.ItemStack

case class InteractionEvents(
  itemRestrictionOps: ItemRestrictionOps,
  onLeftClickBlock: Event[InteractionEvent.LeftClickBlock] = InteractionEvent.LEFT_CLICK_BLOCK,
  onRightClickBlock: Event[InteractionEvent.RightClickBlock] = InteractionEvent.RIGHT_CLICK_BLOCK,
  onRightClickItem: Event[InteractionEvent.RightClickItem] = InteractionEvent.RIGHT_CLICK_ITEM,
  onInteractEntity: Event[InteractionEvent.InteractEntity] = InteractionEvent.INTERACT_ENTITY,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  skipLogger: PlayerSkillsLogger = PlayerSkillsLogger.SKIPS,
) extends EventUtils {
  def registerEvents(): Unit = {
    onLeftClickBlock
      .register { (player: McPlayer, hand: InteractionHand, pos: BlockPos, _: Direction) =>
        beforeUseItemBlock(
          Player(player),
          hand,
          Position(pos),
        )
      }

    onRightClickBlock
      .register { (player: McPlayer, hand: InteractionHand, pos: BlockPos, _: Direction) =>
        beforeUseItemBlock(
          Player(player),
          hand,
          Position(pos),
        )
      }

    onRightClickItem
      .register { (player: McPlayer, hand: InteractionHand) =>
        beforeUseItem(
          Player(player),
          hand,
        )
      }

    onInteractEntity
      .register { (player: McPlayer, entity: McEntity, hand: InteractionHand) =>
        beforeInteractEntity(
          Option(player).map(Player(_)),
          Option(entity).map(Entity(_)),
          hand,
        )
      }
  }

  private[bindings] def beforeUseItem(player: Player[_], hand: InteractionHand): CompoundEventResult[ItemStack] = {
    val result = for {
      item <- player.getItemInHand(hand).filterNot(_.isDefault)
      usable = itemRestrictionOps.isUsable(player, item, None)
    } yield {
      if (!usable) {
        logger.debug(s"${player.name} cannot use ${item.name}")
      } else {
        skipLogger.debug(s"${player.name} is going to use ${item.name}")
      }
      usable
    }
    failCompoundOn[ItemStack](result)
  }

  private[bindings] def beforeUseItemBlock(player: Player[_], hand: InteractionHand, pos: Position): EventResult = {
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
      usable = itemRestrictionOps.isUsable(player, item, Option(pos))
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

  private[bindings] def beforeInteractEntity(
    playerOpt: Option[Player[_]],
    entityOpt: Option[Entity[_]],
    hand: InteractionHand,
  ): EventResult = {
    val result = for {
      player <- playerOpt
      entity <- entityOpt
      item <- player.getItemInHand(hand).filterNot(_.isDefault)
      usable = itemRestrictionOps.isUsable(player, item, None)
    } yield {
      //    val mobType = MobRestrictions.getName(entity.type)
      //    if (!MobRestrictions.canInteractWith(entity.type, player)) {
      //      PlayerSkillsLogger.MOBS.debug("${player.name.string} cannot interact with entity $mobType")
      //      return EventResult.interruptFalse()
      //    }
      if (!usable) {
        logger.debug(s"${player.name} cannot interact with entity ${entity.mobTypeName} using ${item.name}")
      } else {
        skipLogger.debug(s"${player.name} is going to interact with entity ${entity.mobTypeName} using ${item.name}")
      }

      usable
    }

    failOn(result)
  }
}
