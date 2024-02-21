package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.Event
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.NetHandler

case class PlayerEvents(
  playerRegistry: PlayerRegistry = PlayerRegistry(),
  netHandler: NetHandler = NetHandler(),
  onPlayerJoin: Event[PlayerEvent.PlayerJoin] = PlayerEvent.PLAYER_JOIN,
  onPlayerQuit: Event[PlayerEvent.PlayerQuit] = PlayerEvent.PLAYER_QUIT,
) {
  def registerEvents(): Unit = {
    onPlayerJoin.register(p => playerJoin(Player(p)))
    onPlayerQuit.register(p => playerQuit(Player(p)))
  }

  private[bindings] def playerJoin(player: Player[_]): Unit = {
    playerRegistry.open(player.uuid)
    netHandler.syncPlayer(player)
  }

  private[bindings] def playerQuit(player: Player[_]): Unit = {
    playerRegistry.close(player.uuid)
  }
}
