package net.impleri.slab.events

import dev.architectury.event.events.common.TickEvent
import dev.architectury.event.Event
import net.impleri.slab.entity.Player
import net.impleri.slab.server.Server
import net.impleri.slab.world.Level
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.{Player => McPlayer}

sealed trait PlayerTickType

object PlayerTickType {
  final case object Any extends PlayerTickType

  final case object Server extends PlayerTickType

  final case object Client extends PlayerTickType
}

sealed trait PlayerTickTiming

object PlayerTickTiming {
  final case object OneTick extends PlayerTickTiming

  final case object OneSecond extends PlayerTickTiming

  final case object FiveSeconds extends PlayerTickTiming
}

case class TickEvents(
  onServerStartEvent: Event[TickEvent.Server] = TickEvent.SERVER_PRE,
  onServerEndEvent: Event[TickEvent.Server] = TickEvent.SERVER_POST,
  onLevelStartEvent: Event[TickEvent.ServerLevelTick] =
    TickEvent.SERVER_LEVEL_PRE,
  onLevelEndEvent: Event[TickEvent.ServerLevelTick] =
    TickEvent.SERVER_LEVEL_POST,
  onPlayerStartEvent: Event[TickEvent.Player] = TickEvent.PLAYER_PRE,
  onPlayerEndEvent: Event[TickEvent.Player] = TickEvent.PLAYER_POST,
) {
  def onServerStart(f: TickEvents.OnServerTick): Unit =
    onServerStartEvent.register((server: MinecraftServer) =>
      Option(server).map(Server(_)).foreach(f),
    )

  def onServerEnd(f: TickEvents.OnServerTick): Unit =
    onServerEndEvent.register((server: MinecraftServer) =>
      Option(server).map(Server(_)).foreach(f),
    )

  def onLevelStart(f: TickEvents.OnLevelTick): Unit =
    onLevelStartEvent.register((level: ServerLevel) =>
      Option(level).map(Level(_)).foreach(f),
    )

  def onLevelEnd(f: TickEvents.OnLevelTick): Unit =
    onLevelEndEvent.register((level: ServerLevel) =>
      Option(level).map(Level(_)).foreach(f),
    )

  private def isRightTick(tick: Int, time: PlayerTickTiming): Boolean =
    time match {
      case PlayerTickTiming.OneTick => true
      case PlayerTickTiming.OneSecond if TickEvents.isSeconds(1, tick) => true
      case PlayerTickTiming.FiveSeconds if TickEvents.isSeconds(5, tick) => true
      case _ => false
    }

  private def handleTick(
    rawPlayer: McPlayer,
    time: PlayerTickTiming,
    side: PlayerTickType,
    f: TickEvents.OnPlayerTick,
  ): Unit =
    (Option(rawPlayer).map(Player(_)), side) match {
      case (Some(player: Player), PlayerTickType.Client) if player.isClient && isRightTick(player.currentTick, time) => f(player)
      case (Some(player: Player), PlayerTickType.Server) if player.isServer && isRightTick(player.currentTick, time) => f(player)
      case _ => ()
    }

  def onPlayerStart(
    f: TickEvents.OnPlayerTick,
    time: PlayerTickTiming = PlayerTickTiming.OneTick,
    side: PlayerTickType = PlayerTickType.Any,
  ): Unit =
    onPlayerStartEvent.register((rawPlayer: McPlayer) => handleTick(rawPlayer, time, side, f))

  def onPlayerEnd(
    f: TickEvents.OnPlayerTick,
    time: PlayerTickTiming = PlayerTickTiming.OneTick,
    side: PlayerTickType = PlayerTickType.Any,
  ): Unit = {
    onPlayerEndEvent.register((rawPlayer: McPlayer) => handleTick(rawPlayer, time, side, f))
  }
}

object TickEvents {
  type OnServerTick = Server => Unit
  type OnLevelTick = Level.Any => Unit
  type OnPlayerTick = Player => Unit

  final val TICKS_PER_SECOND = 20

  private def isSeconds(sec: Int, tick: Int): Boolean = (tick % (sec * TickEvents.TICKS_PER_SECOND)) == 0
}
