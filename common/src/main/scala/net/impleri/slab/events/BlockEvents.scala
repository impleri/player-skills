package net.impleri.slab.events

import dev.architectury.event.Event
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.EventResult
import dev.architectury.utils.value.IntValue
import net.impleri.slab.block.Block
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Player
import net.impleri.slab.world.Level
import net.impleri.slab.world.Position
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.level.block.state.BlockState

case class BlockEvents(
  private val onBreakEvent: Event[BlockEvent.Break] = BlockEvent.BREAK,
  private val onPlaceEvent: Event[BlockEvent.Place] = BlockEvent.PLACE,
  private val onFallingLandEvent: Event[BlockEvent.FallingLand] =
    BlockEvent.FALLING_LAND,
) {
  def onBreak(f: BlockEvents.OnBreak): Unit = {
    onBreakEvent.register {
      (
        level: McLevel,
        pos: BlockPos,
        state: BlockState,
        player: ServerPlayer,
        xp: IntValue,
      ) =>
        Option(player)
          .map(Player(_))
          .fold(EventResult.pass())(
            f(
              _,
              Option(state).map(Block(_)),
              Option(pos).map(Position(_)),
              Option(level).map(Level(_)),
              Option(xp).map(_.getAsInt),
            ),
          )
    }
  }

  def onPlace(f: BlockEvents.OnPlace): Unit = {
    onPlaceEvent.register {
      (level: McLevel, pos: BlockPos, state: BlockState, entity: McEntity) =>
        Option(state)
          .map(Block(_))
          .fold(EventResult.pass())(
            f(
              _,
              Option(pos).map(Position(_)),
              Option(level).map(Level(_)),
              Option(entity).map(Entity(_)),
            ),
          )
    }
  }

  def onFallingLand(f: BlockEvents.OnFallingLand): Unit = {
    onFallingLandEvent
      .register {
        (
          level: McLevel,
          pos: BlockPos,
          state: BlockState,
          surface: BlockState,
          entity: McEntity,
        ) =>
          Option(state)
            .map(Block(_))
            .foreach(
              f(
                _,
                Option(surface).map(Block(_)),
                Option(pos).map(Position(_)),
                Option(level).map(Level(_)),
                Option(entity).map(Entity(_)),
              ),
            )
      }
  }
}

object BlockEvents {
  type OnBreak = (
    Player.Any,
    Option[Block],
    Option[Position],
    Option[Level.Any],
    Option[Int],
  ) => EventResult
  type OnPlace = (
    Block,
    Option[Position],
    Option[Level.Any],
    Option[Entity.Any],
  ) => EventResult
  type OnFallingLand = (
    Block,
    Option[Block],
    Option[Position],
    Option[Level.Any],
    Option[Entity.Any],
  ) => Unit
}
