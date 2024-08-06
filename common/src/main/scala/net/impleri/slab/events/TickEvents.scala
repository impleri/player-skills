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
  case class Any() extends PlayerTickType

  case class Server() extends PlayerTickType

  case class Client() extends PlayerTickType
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
  def onServerStart(f: TickEvents.OnServerTick): Unit = {
    onServerStartEvent.register((server: MinecraftServer) =>
      Option(server).map(Server(_)).foreach(f),
    )
  }

  def onServerEnd(f: TickEvents.OnServerTick): Unit = {
    onServerEndEvent.register((server: MinecraftServer) =>
      Option(server).map(Server(_)).foreach(f),
    )
  }

  def onLevelStart(f: TickEvents.OnLevelTick): Unit = {
    onLevelStartEvent.register((level: ServerLevel) =>
      Option(level).map(Level(_)).foreach(f),
    )
  }

  def onLevelEnd(f: TickEvents.OnLevelTick): Unit = {
    onLevelEndEvent.register((level: ServerLevel) =>
      Option(level).map(Level(_)).foreach(f),
    )
  }

  def onPlayerStart(
    f: TickEvents.OnPlayerTick,
    side: PlayerTickType = PlayerTickType.Any(),
  ): Unit = {

    onPlayerStartEvent.register((rawPlayer: McPlayer) => {
      val player = Option(rawPlayer).map(Player(_)) flatMap {
        // Skip tick handler if handler wants client-side only and we're server-side
        case p: Player.Any if p.isServer && side != PlayerTickType.Client() =>
          None
        // Skip tick handler if handler wants server-side only and we're client-side
        case p: Player.Any if p.isClient && side == PlayerTickType.Server() =>
          None
        case p: Player.Any => Option(p)
      }

      player.foreach(f)
    })
  }

  def onPlayerEnd(f: TickEvents.OnPlayerTick): Unit = {
    onPlayerEndEvent.register((player: McPlayer) =>
      Option(player).map(Player(_)).foreach(f),
    )
  }
}

object TickEvents {
  type OnServerTick = Server => Unit
  type OnLevelTick = Level.Any => Unit
  type OnPlayerTick = Player.Any => Unit
}
