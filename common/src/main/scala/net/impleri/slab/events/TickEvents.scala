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

  def onPlayerStart(
    f: TickEvents.OnPlayerTick,
    side: PlayerTickType = PlayerTickType.Any,
  ): Unit =
    onPlayerStartEvent.register((rawPlayer: McPlayer) =>
        (Option(rawPlayer).map(Player(_)), side) match {
          case (Some(player: Player), PlayerTickType.Client) if player.isClient => f(player)
          case (Some(player: Player), PlayerTickType.Server) if player.isServer => f(player)
          case _ => ()
        }
    )

  def onPlayerEnd(f: TickEvents.OnPlayerTick): Unit = {
    onPlayerEndEvent.register((player: McPlayer) =>
      Option(player).map(Player(_)).foreach(f),
    )
  }
}

object TickEvents {
  type OnServerTick = Server => Unit
  type OnLevelTick = Level.Any => Unit
  type OnPlayerTick = Player => Unit
}
