package net.impleri.playerskills.server.bindings.lifecycle

import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.slab.events.ServerLifecycleEvents
import net.impleri.slab.server.Server

import scala.annotation.unused

case class BeforeServerStops(
  playerRegistry: PlayerRegistry = PlayerRegistry(),
  upstream: ServerLifecycleEvents = ServerLifecycleEvents(),
) {
  private[bindings] def handler(@unused server: Option[Server]): Unit = {
    playerRegistry.close()
  }

  upstream.beforeServerStop(handler)
}
