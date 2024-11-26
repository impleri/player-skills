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
) extends ResultHandler {
  def onBreak(handler: BlockEvents.OnBreak): Unit =
    onBreakEvent.register {
      (
        rawLevel: McLevel,
        pos: BlockPos,
        state: BlockState,
        serverPlayer: ServerPlayer,
        xp: IntValue,
      ) =>
        ensureResult {
          for {
            player <- Option(serverPlayer).map(Player(_))
            block = Option(state).map(Block(_))
            location = Option(pos).map(Position(_))
            level = Option(rawLevel).map(Level(_))
            xpGain = Option(xp).map(_.getAsInt)
          } yield handler(
            player,
            block,
            location,
            level,
            xpGain,
          )
        }
    }

  def onPlace(handler: BlockEvents.OnPlace): Unit =
    onPlaceEvent.register {
      (
        rawLevel: McLevel,
        pos: BlockPos,
        state: BlockState,
        rawEntity: McEntity,
      ) =>
        ensureResult {
          for {
            block <- Option(state).map(Block(_))
            location = Option(pos).map(Position(_))
            level = Option(rawLevel).map(Level(_))
            entity = Option(rawEntity).map(Entity(_))
          } yield handler(
            block,
            location,
            level,
            entity,
          )
        }
    }

  def onFallingLand(handler: BlockEvents.OnFallingLand): Unit =
    onFallingLandEvent
      .register {
        (
          rawLevel: McLevel,
          pos: BlockPos,
          state: BlockState,
          surface: BlockState,
          rawEntity: McEntity,
        ) =>
          for {
            block <- Option(state).map(Block(_))
            onBlock = Option(surface).map(Block(_))
            location = Option(pos).map(Position(_))
            level = Option(rawLevel).map(Level(_))
            entity = Option(rawEntity).map(Entity(_))
          } yield handler(
            block,
            onBlock,
            location,
            level,
            entity,
          )
      }
}

object BlockEvents {
  type OnBreak = (
    Player,
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
