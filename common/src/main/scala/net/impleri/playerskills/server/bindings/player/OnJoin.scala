package net.impleri.playerskills.server.bindings.player

import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.NetHandler
import net.impleri.slab.events.PlayerEvents

case class OnJoin(
  playerRegistry: () => PlayerRegistry = () => PlayerRegistry(),
  netHandler: NetHandler = NetHandler(),
  upstream: PlayerEvents = PlayerEvents(),
) {
  private[bindings] val handler: PlayerEvents.OnJoinOrQuit = player => {
    playerRegistry().open(player.uuid)
    netHandler.syncPlayer(player)
  }

  upstream.onJoin(handler)
}
