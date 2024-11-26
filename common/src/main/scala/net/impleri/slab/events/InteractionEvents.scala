package net.impleri.slab.events

import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.CompoundEventResult
import dev.architectury.event.Event
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.InteractionEvent.FarmlandTrample
import dev.architectury.event.events.common.InteractionEvent.InteractEntity
import dev.architectury.event.events.common.InteractionEvent.LeftClickBlock
import dev.architectury.event.events.common.InteractionEvent.RightClickBlock
import dev.architectury.event.events.common.InteractionEvent.RightClickItem
import net.impleri.slab.block.Block
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.world.Direction
import net.impleri.slab.world.Position
import net.minecraft.core.{Direction => McDirection}
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

case class InteractionEvents(
  private val onLeftClickBlockEvent: Event[LeftClickBlock] =
    InteractionEvent.LEFT_CLICK_BLOCK,
  private val onRightClickBlockEvent: Event[RightClickBlock] =
    InteractionEvent.RIGHT_CLICK_BLOCK,
  private val onRightClickItemEvent: Event[RightClickItem] =
    InteractionEvent.RIGHT_CLICK_ITEM,
  private val onInteractEntityEvent: Event[InteractEntity] =
    InteractionEvent.INTERACT_ENTITY,
  private val onFarmlandTrampleEvent: Event[FarmlandTrample] =
    InteractionEvent.FARMLAND_TRAMPLE,
) extends ResultHandler {
  private def onBlockClick(
        rawPlayer: McPlayer,
        rawHand: InteractionHand,
        pos: BlockPos,
        d: McDirection,
        handler: InteractionEvents.OnClickBlock,
      ) =
        ensureResult {
          for {
            player <- Option(rawPlayer).map(Player(_))
            location = Option(pos).map(Position(_))
            hand = Hand.fromVanilla(rawHand)
            direction = Direction.fromVanilla(d)
          } yield handler(player, location, hand, direction)
        }

  def onLeftClickBlock(handler: InteractionEvents.OnClickBlock): Unit =
    onLeftClickBlockEvent.register {
      (
        rawPlayer: McPlayer,
        rawHand: InteractionHand,
        pos: BlockPos,
        d: McDirection,
      ) => onBlockClick(rawPlayer, rawHand, pos, d, handler)
    }

  def onRightClickBlock(handler: InteractionEvents.OnClickBlock): Unit =
    onRightClickBlockEvent.register {
      (
        rawPlayer: McPlayer,
        rawHand: InteractionHand,
        pos: BlockPos,
        d: McDirection,
      ) => onBlockClick(rawPlayer, rawHand, pos, d, handler)
    }

  def onRightClickItem(handler: InteractionEvents.OnUseItem): Unit =
    onRightClickItemEvent.register {
      (rawPlayer: McPlayer, rawHand: InteractionHand) =>
        ensureCompoundResult[ItemStack] {
          for {
            player <- Option(rawPlayer).map(Player(_))
            hand = Hand.fromVanilla(rawHand)
          } yield handler(player, hand)
        }
    }

  def onRightClickEntity(handler: InteractionEvents.OnClickEntity): Unit =
    onInteractEntityEvent.register {
      (rawPlayer: McPlayer, entity: McEntity, rawHand: InteractionHand) =>
        ensureResult {
          for {
            player <- Option(rawPlayer).map(Player(_))
            target = Option(entity).map(Entity(_))
            hand = Hand.fromVanilla(rawHand)
          } yield handler(player, target, hand)
        }
    }

  def onTrample(handler: InteractionEvents.OnTrampleBlock): Unit =
    onFarmlandTrampleEvent.register {
      (
        level: Level,
        pos: BlockPos,
        state: BlockState,
        rawDistance: Float,
        entity: McEntity,
      ) =>
        ensureResult {
          for {
            actor <- Option(entity).map(Entity(_))
            location = Option(pos).map(Position(_))
            block = Option(state).map(Block(_))
            distance = Option(rawDistance).getOrElse(0.0f)
          } yield handler(actor, location, block, distance)
        }
    }
}

object InteractionEvents {
  type OnClickBlock =
    (Player, Option[Position], Hand.Hand, Direction.Direction) => EventResult
  type OnClickEntity = (Player, Option[Entity[_]], Hand.Hand) => EventResult
  type OnTrampleBlock =
    (Entity[_], Option[Position], Option[Block], Float) => EventResult
  type OnUseItem = (Player, Hand.Hand) => CompoundEventResult[ItemStack]
}
