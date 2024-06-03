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
  private val onLeftClickBlockEvent: Event[LeftClickBlock] = InteractionEvent.LEFT_CLICK_BLOCK,
  private val onRightClickBlockEvent: Event[RightClickBlock] = InteractionEvent.RIGHT_CLICK_BLOCK,
  private val onRightClickItemEvent: Event[RightClickItem] = InteractionEvent.RIGHT_CLICK_ITEM,
  private val onInteractEntityEvent: Event[InteractEntity] = InteractionEvent.INTERACT_ENTITY,
  private val onFarmlandTrampleEvent: Event[FarmlandTrample] = InteractionEvent.FARMLAND_TRAMPLE,
) {
  private def handleEvent[Result](player: McPlayer, fallbackResult: Result = EventResult.pass())
    (f: Player[_] => Result): Result = {
    Option(player)
      .map(Player(_))
      .fold(fallbackResult)(f)
  }

  def onLeftClickBlock(handler: InteractionEvents.OnClickBlock): Unit = {
    onLeftClickBlockEvent.register { (player: McPlayer, hand: InteractionHand, pos: BlockPos, d: McDirection) =>
      handleEvent(player)(
        handler(
          _,
          Option(pos).map(Position(_)),
          Hand.fromVanilla(hand),
          Direction.fromVanilla(d),
        ),
      )
    }
  }

  def onRightClickBlock(handler: InteractionEvents.OnClickBlock): Unit = {
    onRightClickBlockEvent.register { (player: McPlayer, hand: InteractionHand, pos: BlockPos, d: McDirection) =>
      handleEvent(player)(
        handler(
          _,
          Option(pos).map(Position(_)),
          Hand.fromVanilla(hand),
          Direction.fromVanilla(d),
        ),
      )
    }
  }

  def onRightClickItem(handler: InteractionEvents.OnUseItem): Unit = {
    onRightClickItemEvent.register { (player: McPlayer, hand: InteractionHand) =>
      handleEvent(player, CompoundEventResult.pass[ItemStack]())(
        handler(
          _,
          Hand.fromVanilla(hand),
        ),
      )
    }
  }

  def onRightClickEntity(handler: InteractionEvents.OnClickEntity): Unit = {
    onInteractEntityEvent.register { (player: McPlayer, entity: McEntity, hand: InteractionHand) =>
      handleEvent(player)(
        handler(
          _,
          Option(entity).map(Entity(_)),
          Hand.fromVanilla(hand),
        ),
      )
    }
  }

  def onTrample(handler: InteractionEvents.OnTrampleBlock): Unit = {
    onFarmlandTrampleEvent.register {
      (level: Level, pos: BlockPos, state: BlockState, distance: Float, entity: McEntity) =>
      Option(entity)
        .map(Entity(_))
        .fold(EventResult.pass())(handler(
          _,
          Option(pos).map(Position(_)),
          Option(state).map(Block(_)),
          Option(distance).getOrElse(0.0F),
        ),
        )
    }
  }
}

object InteractionEvents {
  type OnClickBlock = (Player[_], Option[Position], Hand.Hand, Direction.Direction) => EventResult
  type OnClickEntity = (Player[_], Option[Entity[_]], Hand.Hand) => EventResult
  type OnTrampleBlock = (Entity[_], Option[Position], Option[Block], Float) => EventResult
  type OnUseItem = (Player[_], Hand.Hand) => CompoundEventResult[ItemStack]
}
